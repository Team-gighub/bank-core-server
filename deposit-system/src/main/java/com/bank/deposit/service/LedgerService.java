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

    //당행 기록 로직
    @Transactional
    public void recodeSameBank(EscrowAccount escrowAccount){
        //1. 에스크로 계좌 정보 기록 -> 예치
        Ledger Escrowledger = new Ledger();
        Escrowledger.setAccountId(escrowAccount.getEscrowAccountId());
        Escrowledger.setTransactionType(TransactionType.DEPOSIT);
        Escrowledger.setDebitCredit(DebitCredit.DEBIT);
        Escrowledger.setAmount(escrowAccount.getHoldAmount());
        Escrowledger.setBalanceBefore(BigDecimal.ZERO);
        Escrowledger.setBalanceAfter(escrowAccount.getHoldAmount());
        Escrowledger.setCounterpartyBankCode("020");
        Escrowledger.setCounterpartyAccount(escrowAccount.getPayerAccount());
        Escrowledger.setCounterpartyName(escrowAccount.getPayerName());
        Escrowledger.setDescription(Description.에스크로계좌입금);
        Escrowledger.setTransactionDatetime(LocalDateTime.now());
        Escrowledger.setValueDate(LocalDate.now());
        Escrowledger.setChannelType(ChannelType.BAAS);
        ledgerRepository.save(Escrowledger);

        // 2. 당행 계좌 정보 기록 -> 출금
        Ledger Userledger = new Ledger();
        Userledger.setAccountId(escrowAccount.getBankAccount().getAccountId());
        Userledger.setTransactionType(TransactionType.WITHDRAWAL);
        Userledger.setDebitCredit(DebitCredit.CREDIT);
        Userledger.setAmount(escrowAccount.getHoldAmount());
        Userledger.setBalanceBefore(escrowAccount.getBankAccount().getBalance());
        Userledger.setBalanceAfter(escrowAccount.getBankAccount().getBalance().subtract(escrowAccount.getHoldAmount()));
        Userledger.setCounterpartyBankCode("020");
        Userledger.setCounterpartyAccount(escrowAccount.getPayerAccount());
        Userledger.setCounterpartyName(escrowAccount.getPayerName());
        Userledger.setDescription(Description.사용자계좌출금);
        Userledger.setTransactionDatetime(LocalDateTime.now());
        Userledger.setValueDate(LocalDate.now());
        Userledger.setChannelType(ChannelType.BAAS);
        ledgerRepository.save(Userledger);

        //3. 사용자 계좌 잔액 변경(기존잔액 - hold 금액)
        String accoutId = escrowAccount.getBankAccount().getAccountId();
        Account account = accountRepository.findById(accoutId).orElseThrow(()->new CustomException(ErrorCode.INVALID_INPUT_VALUE));
        account.setBalance(account.getBalance().subtract(escrowAccount.getHoldAmount()));

    }

    // 타행 기록 로직
    @Transactional
    public void recodeDifferentBank(EscrowAccount escrowAccount){
        Ledger ledger = new Ledger();
        //TODO : 금결원 호출
        externalTransferPort.send("결제요청");
        //ok 로 가정
        //에스크로 계좌 정보 저장
        ledger.setAccountId(escrowAccount.getEscrowAccountId());
        ledger.setTransactionType(TransactionType.DEPOSIT);
        ledger.setDebitCredit(DebitCredit.DEBIT);
        ledger.setAmount(escrowAccount.getHoldAmount());
        ledger.setBalanceBefore(BigDecimal.ZERO);
        ledger.setBalanceAfter(escrowAccount.getHoldAmount());
        ledger.setCounterpartyBankCode("020");
        ledger.setCounterpartyAccount(escrowAccount.getPayerAccount());
        ledger.setCounterpartyName(escrowAccount.getPayerName());
        ledger.setDescription(Description.에스크로계좌입금);
        ledger.setTransactionDatetime(LocalDateTime.now());
        ledger.setValueDate(LocalDate.now());
        ledger.setChannelType(ChannelType.BAAS);
        ledgerRepository.save(ledger);
    }
}
