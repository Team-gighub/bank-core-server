package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.common.port.ExternalDepositPort;
import com.bank.common.port.ExternalDepositValidatePort;
import com.bank.common.util.TraceIdUtil;
import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.Ledger;
import com.bank.deposit.domain.enums.*;
import com.bank.deposit.dto.EscrowReleaseRequest;
import com.bank.deposit.dto.EscrowReleaseResponse;
import com.bank.deposit.repository.AccountRepository;
import com.bank.deposit.repository.EscrowAccountRepository;
import com.bank.deposit.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmReleaseService {

    private final EscrowAccountRepository escrowAccountRepository;
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final ExternalDepositValidatePort  externalValidatePort;
    private final ExternalDepositPort externalDepositPort;

    /**
     * 지급확정 메인 로직
     */
    public EscrowReleaseResponse confirmRelease(EscrowReleaseRequest request) {
        String traceId = TraceIdUtil.getTraceId();
        log.info("[{}] 지급확정 시작 - escrowId: {}, merchantId: {}", 
                traceId, request.getEscrowId(), request.getMerchantId());

        // 1. 에스크로 계좌 조회
        EscrowAccount escrowAccount = escrowAccountRepository
                .findWithLockByEscrowAccountId(request.getEscrowId())
                .orElseThrow(() -> new CustomException(ErrorCode.ESCROW_ACCOUNT_NOT_FOUND));
        
        log.info("[{}] 에스크로 계좌 조회 완료 - escrowId: {}, holdAmount: {}, paymentAmount: {}", 
                traceId, escrowAccount.getEscrowAccountId(), 
                escrowAccount.getHoldAmount(), escrowAccount.getPaymentAmount());

        // 2. 비즈니스 검증
        validateEscrowForRelease(escrowAccount);
        validateMerchant(escrowAccount, request.getMerchantId());
        
        log.info("[{}] 비즈니스 검증 완료", traceId);

        // 3. 금액 정보 준비
        BigDecimal holdAmount = escrowAccount.getHoldAmount();
        BigDecimal paymentAmount = escrowAccount.getPaymentAmount(); // 수취인 실수령액
        BigDecimal platformFeeAmount = holdAmount.subtract(paymentAmount); // 플랫폼 수수료

        // 3-1. 금액 검증
        validateAmounts(holdAmount, paymentAmount, platformFeeAmount);
        
        log.info("[{}] 금액 정보 - holdAmount: {}, paymentAmount: {}, platformFeeAmount: {}", 
                traceId, holdAmount, paymentAmount, platformFeeAmount);

        // 4. 플랫폼(고객사) 계좌 조회 및 검증 (수수료 입금 준비)
        Account platformAccount = getPlatformAccountWithLock(request.getMerchantId());
        validatePlatformAccount(platformAccount);
        
        log.info("[{}] 플랫폼 계좌 조회 완료 - accountNumber: {}, balance: {}", 
                traceId, platformAccount.getAccountNumber(), platformAccount.getBalance());

        // 5. 당행/타행 분기 - 수취인 지급 처리 먼저
        Ledger escrowToPayeeLedger;
        if (isSameBank(escrowAccount.getPayeeBankCode())) {
            log.info("[{}] 당행 이체 시작 - payeeBankCode: {}, payeeAccount: {}", 
                    traceId, escrowAccount.getPayeeBankCode(), escrowAccount.getPayeeAccount());
            escrowToPayeeLedger = processInternalTransfer(escrowAccount, paymentAmount, platformFeeAmount);
        } else {
            log.info("[{}] 타행 이체 시작 - payeeBankCode: {}, payeeAccount: {}", 
                    traceId, escrowAccount.getPayeeBankCode(), escrowAccount.getPayeeAccount());
            escrowToPayeeLedger = processExternalTransfer(escrowAccount, paymentAmount, platformFeeAmount);
        }
        
        log.info("[{}] 수취인 지급 처리 완료 - ledgerSeq: {}", traceId, escrowToPayeeLedger.getLedgerSeq());

        // 6. 플랫폼 수수료 처리 (당행/타행 무관 - 수취인 지급 후)
        BigDecimal platformBalanceBefore = platformAccount.getBalance();
        platformAccount.deposit(platformFeeAmount);
        BigDecimal platformBalanceAfter = platformAccount.getBalance();
        
        log.info("[{}] 플랫폼 수수료 입금 완료 - platformFeeAmount: {}, balanceBefore: {}, balanceAfter: {}", 
                traceId, platformFeeAmount, platformBalanceBefore, platformBalanceAfter);

        createEscrowToPlatformWithdrawalLedger(escrowAccount, platformAccount, platformFeeAmount);
        createPlatformDepositLedger(
                platformAccount,
                escrowAccount,
                platformFeeAmount,
                platformBalanceBefore,
                platformBalanceAfter
        );

        // 7. 에스크로 계좌 해지
        escrowAccount.release();
        log.info("[{}] 에스크로 계좌 해지 완료 - escrowId: {}", traceId, escrowAccount.getEscrowAccountId());

        // 8. 응답 생성
        log.info("[{}] 지급확정 완료 - escrowId: {}, ledgerSeq: {}", 
                traceId, request.getEscrowId(), escrowToPayeeLedger.getLedgerSeq());
        
        return EscrowReleaseResponse.fromLedgerSeq(escrowToPayeeLedger.getLedgerSeq());
    }

    /**
     * 당행 수취인 지급 처리
     */
    private Ledger processInternalTransfer(
            EscrowAccount escrowAccount,
            BigDecimal paymentAmount,
            BigDecimal platformFeeAmount
    ) {
        String traceId = TraceIdUtil.getTraceId();
        
        // 1. 수취인 계좌 조회 및 검증
        Account payeeAccount = accountRepository
                .findWithLockByAccountNumber(escrowAccount.getPayeeAccount())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYEE_ACCOUNT_NOT_FOUND));

        validatePayeeAccount(payeeAccount);
        
        log.info("[{}] 당행 수취인 계좌 조회 완료 - accountNumber: {}, balance: {}", 
                traceId, payeeAccount.getAccountNumber(), payeeAccount.getBalance());

        // 2. 잔액 정보 캡처
        BigDecimal payeeBalanceBefore = payeeAccount.getBalance();

        // 3. 수취인 계좌 입금
        payeeAccount.deposit(paymentAmount);
        BigDecimal payeeBalanceAfter = payeeAccount.getBalance();
        
        log.info("[{}] 당행 수취인 입금 완료 - paymentAmount: {}, balanceBefore: {}, balanceAfter: {}", 
                traceId, paymentAmount, payeeBalanceBefore, payeeBalanceAfter);

        // 4. 원장 기록 생성 (2개)
        // 4-1. 에스크로 → 수취인 출금
        Ledger escrowToPayeeLedger = createEscrowToPayeeWithdrawalLedger(
                escrowAccount,
                paymentAmount,
                platformFeeAmount
        );

        // 4-2. 수취인 ← 에스크로 입금
        createPayeeDepositLedger(
                payeeAccount,
                escrowAccount,
                paymentAmount,
                payeeBalanceBefore,
                payeeBalanceAfter
        );
        
        log.info("[{}] 당행 이체 원장 기록 완료 - ledgerSeq: {}", traceId, escrowToPayeeLedger.getLedgerSeq());

        return escrowToPayeeLedger;
    }

    /**
     * 타행 수취인 지급 처리
     */
    private Ledger processExternalTransfer(
            EscrowAccount escrowAccount,
            BigDecimal paymentAmount,
            BigDecimal platformFeeAmount
    ) {
        String bankCode = escrowAccount.getPayeeBankCode();
        String accountNumber = escrowAccount.getPayeeAccount();
        String traceId = TraceIdUtil.getTraceId();
        
        log.info("[{}] 타행 이체 검증 시작 - bankCode: {}, accountNumber: {}, amount: {}", 
                traceId, bankCode, accountNumber, paymentAmount);

        // 1. 타행 이체 가능 검증
        if (!externalValidatePort.isDepositPossible(bankCode, accountNumber, paymentAmount, traceId)) {
            log.error("[{}] 타행 이체 불가 - bankCode: {}, accountNumber: {}", 
                    traceId, bankCode, accountNumber);
            throw new CustomException(ErrorCode.EXTERNAL_DEPOSIT_NOT_POSSIBLE);
        }
        
        log.info("[{}] 타행 이체 가능 검증 완료", traceId);

        // 2. 타행 입금 요청
        if (!externalDepositPort.isDepositSuccess(bankCode, accountNumber, paymentAmount, traceId)) {
            log.error("[{}] 타행 입금 실패 - bankCode: {}, accountNumber: {}, amount: {}", 
                    traceId, bankCode, accountNumber, paymentAmount);
            throw new CustomException(ErrorCode.EXTERNAL_DEPOSIT_FAILED);
        }
        
        log.info("[{}] 타행 입금 성공 - bankCode: {}, accountNumber: {}, amount: {}", 
                traceId, bankCode, accountNumber, paymentAmount);

        // 3. 원장 기록 생성
        Ledger ledger = createEscrowToPayeeWithdrawalLedger(escrowAccount, paymentAmount, platformFeeAmount);
        
        log.info("[{}] 타행 이체 원장 기록 완료 - ledgerSeq: {}", traceId, ledger.getLedgerSeq());
        
        return ledger;
    }

    /**
     * 고객사(플랫폼) 계좌 조회
     */
    private Account getPlatformAccountWithLock(String merchantId) {
        return accountRepository
                .findWithLockByUserItscnoAndAccountType(merchantId, AccountType.CHECKING)
                .orElseThrow(() -> new CustomException(ErrorCode.PLATFORM_ACCOUNT_NOT_FOUND));
    }

    /**
     * 고객사 검증
     */
    private void validateMerchant(EscrowAccount escrowAccount, String requestMerchantId) {
        if (!requestMerchantId.equals(escrowAccount.getMerchantId())) {
            throw new CustomException(ErrorCode.MERCHANT_ID_MISMATCH);
        }
    }

    /**
     * 에스크로 해지 가능 여부 검증
     */
    private void validateEscrowForRelease(EscrowAccount escrowAccount) {
        if (escrowAccount.getHoldStatus() == HoldStatus.RELEASED) {
            throw new CustomException(ErrorCode.ESCROW_ALREADY_RELEASED);
        }

        if (escrowAccount.getHoldStatus() == HoldStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ESCROW_ALREADY_CANCELLED);
        }

        if (escrowAccount.getHoldStatus() != HoldStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ESCROW_NOT_ACTIVE);
        }

        if (escrowAccount.getHoldAmount() == null
                || escrowAccount.getHoldAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_ESCROW_AMOUNT);
        }
    }

    /**
     * 수취인 계좌 검증
     */
    private void validatePayeeAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException(ErrorCode.PAYEE_ACCOUNT_NOT_ACTIVE);
        }
    }

    /**
     * 플랫폼 계좌 검증
     */
    private void validatePlatformAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException(ErrorCode.PLATFORM_ACCOUNT_NOT_ACTIVE);
        }
    }

    /**
     * 금액 정합성 검증
     */
    private void validateAmounts(
            BigDecimal holdAmount,
            BigDecimal paymentAmount,
            BigDecimal platformFeeAmount
    ) {
        // null 체크
        if (holdAmount == null || paymentAmount == null) {
            throw new CustomException(ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        // 음수 체크
        if (holdAmount.compareTo(BigDecimal.ZERO) <= 0
                || paymentAmount.compareTo(BigDecimal.ZERO) <= 0
                || platformFeeAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        // 금액 합계 검증: holdAmount = paymentAmount + platformFeeAmount
        BigDecimal sum = paymentAmount.add(platformFeeAmount);
        if (holdAmount.compareTo(sum) != 0) {
            throw new CustomException(ErrorCode.AMOUNT_MISMATCH);
        }
    }

    /**
     * 당행 여부 판단
     */
    private boolean isSameBank(String bankCode) {
        return BankCode.OUR_BANK.getCode().equals(bankCode);
    }

    /**
     * 에스크로 → 수취인 출금 원장
     */
    private Ledger createEscrowToPayeeWithdrawalLedger(
            EscrowAccount escrowAccount,
            BigDecimal paymentAmount,
            BigDecimal platformFeeAmount
    ) {
        Ledger ledger = Ledger.create(
                escrowAccount.getBankAccount().getAccountId(),
                TransactionType.ESCROW_RELEASE,
                DebitCredit.CREDIT, // 대변(에스크로 계좌 입장)
                paymentAmount, // 거래금액 (수취인 실수령액)
                escrowAccount.getHoldAmount(), // 에스크로 지급 전 잔액
                platformFeeAmount, // 실수령액 지급 후 잔액 (수수료만 남음)
                escrowAccount.getPayeeBankCode(),
                escrowAccount.getPayeeAccount(),
                escrowAccount.getPayeeName(),
                Description.에스크로지급확정_에스크로출금_수취인
        );

        return ledgerRepository.save(ledger);
    }

    /**
     * 수취인 ← 에스크로 입금 원장
     */
    private void createPayeeDepositLedger(
            Account payeeAccount,
            EscrowAccount escrowAccount,
            BigDecimal paymentAmount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter
    ) {
        Ledger ledger = Ledger.create(
                payeeAccount.getAccountId(),
                TransactionType.ESCROW_RELEASE,
                DebitCredit.DEBIT, // 차변(수취인 계좌 입장)
                paymentAmount, // 거래금액 (수취인 실수령액)
                balanceBefore, // 거래 전 금액 (수취인 실수령액 입금 전 잔액)
                balanceAfter, // 거래 후 금액 (수취인 실수령액 입금 후 잔액)
                escrowAccount.getPayerBankCode(),
                escrowAccount.getPayerAccount(),
                escrowAccount.getPayerName(),
                Description.에스크로지급확정_수취인입금
        );

        ledgerRepository.save(ledger);
    }

    /**
     * 에스크로 → 플랫폼 출금 원장
     */
    private void createEscrowToPlatformWithdrawalLedger(
            EscrowAccount escrowAccount,
            Account platformAccount,
            BigDecimal platformFeeAmount
    ) {
        Ledger ledger = Ledger.create(
                escrowAccount.getBankAccount().getAccountId(),
                TransactionType.PLATFORM_FEE,
                DebitCredit.CREDIT, // 대변 (에스크로 계좌입장)
                platformFeeAmount, // 거래금액 (플랫폼에게 입금될 수수료)
                platformFeeAmount, // 거래 전 잔액 (에스크로 계좌에서 수취인에게 실수령액 지급 후 남은 수수료)
                BigDecimal.ZERO, // 거래 후 잔액 (0원 - 최종 해지)
                BankCode.OUR_BANK.getCode(),
                platformAccount.getAccountNumber(),
                platformAccount.getUser().getCustomerName(),
                Description.에스크로지급확정_에스크로출금_수수료
        );

        ledgerRepository.save(ledger);
    }

    /**
     * 플랫폼 ← 에스크로 입금 원장
     */
    private void createPlatformDepositLedger(
            Account platformAccount,
            EscrowAccount escrowAccount,
            BigDecimal platformFeeAmount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter
    ) {
        Ledger ledger = Ledger.create(
                platformAccount.getAccountId(),
                TransactionType.PLATFORM_FEE,
                DebitCredit.DEBIT, // 차변 (플랫폼 계좌 입장)
                platformFeeAmount, // 거래 금액 (플랫폼이 얻을 수수료)
                balanceBefore, // 거래 전 잔액 (플랫폼 계좌의 수수료 입금 전 잔액)
                balanceAfter, // 거래 후 잔액 (플랫폼 계좌의 수수료 입금 후 잔액)
                escrowAccount.getPayerBankCode(),
                escrowAccount.getPayerAccount(),
                escrowAccount.getPayerName(),
                Description.에스크로지급확정_플랫폼수수료입금
        );

        ledgerRepository.save(ledger);
    }

}