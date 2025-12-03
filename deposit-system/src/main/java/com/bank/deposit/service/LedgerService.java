package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.common.port.ExternalWithdrawPort;
import com.bank.common.util.TraceIdUtil;
import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.Ledger;
import com.bank.deposit.domain.enums.DebitCredit;
import com.bank.deposit.domain.enums.Description;
import com.bank.deposit.domain.enums.TransactionType;
import com.bank.deposit.repository.AccountRepository;
import com.bank.deposit.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final ExternalWithdrawPort externalWithdrawPort;
    private final AccountRepository accountRepository;

    private Ledger createEscrowDepositLedger(EscrowAccount escrowAccount) {
        return Ledger.create(
                escrowAccount.getEscrowAccountId(),
                TransactionType.DEPOSIT,
                DebitCredit.DEBIT,
                escrowAccount.getHoldAmount(),
                BigDecimal.ZERO,
                escrowAccount.getHoldAmount(),
                escrowAccount.getPayerBankCode(),
                escrowAccount.getPayerAccount(),
                escrowAccount.getPayerName(),
                Description.에스크로계좌입금
        );
    }

    // 당행 기록 로직
    @Transactional
    public void recodeSameBank(EscrowAccount escrowAccount) {

        log.info("당행 원장 기록 시작 traceId={}", TraceIdUtil.getTraceId());

        // 1. 에스크로 계좌 Ledger (입금)
        Ledger escrowLedger = createEscrowDepositLedger(escrowAccount);
        ledgerRepository.save(escrowLedger);

        // 2. 사용자 계좌 Ledger (출금)
        Account account = accountRepository.findByAccountNumber(escrowAccount.getPayerAccount())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));
        Ledger userLedger =  Ledger.create(
                account.getAccountId(),
                TransactionType.WITHDRAWAL,
                DebitCredit.CREDIT,
                escrowAccount.getHoldAmount(),
                account.getBalance(),
                account.getBalance().subtract(escrowAccount.getHoldAmount()),
                escrowAccount.getPayerBankCode(),
                escrowAccount.getPayerAccount(),
                escrowAccount.getPayerName(),
                Description.사용자계좌출금
        );
        ledgerRepository.save(userLedger);
        log.info("당행 원장 기록 끝 traceId={}", TraceIdUtil.getTraceId());


        // 3. 사용자 계좌 잔액 변경
        account.withdraw(escrowAccount.getHoldAmount());
    }


    // 타행 기록 로직
    @Transactional
    public void recodeDifferentBank(EscrowAccount escrowAccount) {
        log.info("타행 원장 기록 시작 traceId={}", TraceIdUtil.getTraceId());
        // 대외계 호출 로직
        boolean result = externalWithdrawPort.isWithdrawalSuccess(escrowAccount.getPayerBankCode(),escrowAccount.getPayerAccount(),escrowAccount.getHoldAmount(),TraceIdUtil.getTraceId());
        if(!result){
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        Ledger escrowLedger = createEscrowDepositLedger(escrowAccount);

        ledgerRepository.save(escrowLedger);
        log.info("타행 원장 기록 끝 traceId={}", TraceIdUtil.getTraceId());

    }

}
