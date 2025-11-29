package com.bank.external.adapter;

import com.bank.common.port.ExternalValidatePort;
import com.bank.external.kftc.MockOpenBanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalValidatePortImpl implements ExternalValidatePort {

    private final MockOpenBanking openBanking;

    @Override
    public boolean isWithdrawalPossible(String bankCode, String accountNo, BigDecimal amount) {
        log.info("외부 계좌 출금 가능 여부 조회 → {}, {}, {}", bankCode, accountNo, amount);
        return openBanking.isAccountValid(bankCode, accountNo, amount);

    }
}
