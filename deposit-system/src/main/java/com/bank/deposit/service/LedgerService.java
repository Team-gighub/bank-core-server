package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.common.port.ExternalTransferPort;
import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.Ledger;
import com.bank.deposit.domain.enums.ChannelType;
import com.bank.deposit.domain.enums.DebitCredit;
import com.bank.deposit.domain.enums.Description;
import com.bank.deposit.domain.enums.TransactionType;
import com.bank.deposit.repository.AccountRepository;
import com.bank.deposit.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final ExternalTransferPort externalTransferPort;
    private final AccountRepository accountRepository;
    //로직 분리 -> leder 기록
    private Ledger createLedger(
            String accountId,
            TransactionType transactionType,
            DebitCredit debitCredit,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String counterpartyBankCode,
            String counterpartyAccount,
            String counterpartyName,
            Description description) {

        return Ledger.builder()
                .accountId(accountId)
                .transactionType(transactionType)
                .debitCredit(debitCredit)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .counterpartyBankCode(counterpartyBankCode)
                .counterpartyAccount(counterpartyAccount)
                .counterpartyName(counterpartyName)
                .description(description)
                .transactionDatetime(LocalDateTime.now())
                .valueDate(LocalDate.now())
                .channelType(ChannelType.BAAS)
                .build();
    }

    // 당행 기록 로직
    @Transactional
    public void recodeSameBank(EscrowAccount escrowAccount) {

        // 1. 에스크로 계좌 Ledger (입금)
        Ledger escrowLedger = createLedger(
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
        ledgerRepository.save(escrowLedger);

        // 2. 사용자 계좌 Ledger (출금)
        Account account = accountRepository.findAndLockById(escrowAccount.getBankAccount().getAccountId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        Ledger userLedger = createLedger(
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

        // 3. 사용자 계좌 잔액 변경
        account.setBalance(account.getBalance().subtract(escrowAccount.getHoldAmount()));
    }


    // 타행 기록 로직
    @Transactional
    public void recodeDifferentBank(EscrowAccount escrowAccount) {

        // TODO 외부이체 호출
        externalTransferPort.send("결제요청");

        Ledger ledger = createLedger(
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
        ledgerRepository.save(ledger);
    }

}
