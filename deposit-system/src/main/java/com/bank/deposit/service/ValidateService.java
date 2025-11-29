package com.bank.deposit.service;

import com.bank.common.port.ExternalValidatePort;
import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.enums.AccountStatus;
import com.bank.deposit.domain.enums.BankCode;
import com.bank.deposit.dto.PayerInfoDto;
import com.bank.deposit.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateService {

    private final AccountRepository accountRepository;
    private final ExternalValidatePort externalValidatePort;

    public void validatePayer(PayerInfoDto payer, BigDecimal amount) {

        // 1-1. 계좌의 은행이 당행인지 타행인지 식별 (은행코드 기준)
        boolean isSameBank = isSameBank(payer.getBankCode());


        // 1-2. 계좌 인증 검증
        boolean isAccountActive = isSameBank
                ? checkInternalAccountValidate(payer.getAccountNo(), amount)
                : checkExternalAccountValidate(payer.getBankCode(), payer.getAccountNo(), amount);

        if (!isAccountActive) {
            throw new RuntimeException("출금 불가");
        }

        log.info("{} validate success" , payer.getAccountNo());
    }



    // 1. 당행/타행 판단
    private boolean isSameBank(String payerBankCode) {
        return BankCode.OUR_BANK.getCode().equals(payerBankCode);
    }


    // 2. 당행 계좌 출금 인증 검증
    private boolean checkInternalAccountValidate(String accountNo, BigDecimal amount) {
        log.info("{} check internal account validate" , accountNo);

        // 1-2. 계좌 상태 조회 (DB에서 상태 확인)
        Optional<Account> optAccount = accountRepository.findByAccountNumber(accountNo);
        if (optAccount.isEmpty()) {
            throw new RuntimeException("계좌를 찾을 수 없습니다.");
        }

        Account account = optAccount.get();

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("계좌 상태가 비정상입니다.");
        }

        log.info("check internal account status {}" , account.getStatus());

        // 1-5. 잔액 검증
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("잔액이 부족합니다.");
        }
        log.debug("internal account balance: {}, required amount: {}", account.getBalance(), amount);

        return true;
    }

    // 2. 타행 계좌 출금 인증 검증
    private boolean checkExternalAccountValidate(String bankCode, String accountNo, BigDecimal amount) {
        log.info("{} check external account validate" , accountNo);
        return externalValidatePort.isWithdrawalPossible(bankCode, accountNo, amount);
    }


}
