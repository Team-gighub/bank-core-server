package com.bank.external.adapter;

import com.bank.common.port.ExternalDepositValidatePort;
import com.bank.external.kftc.MockCommonBanking;
import com.bank.external.kftc.MockOpenBanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalDepositValidatePortImpl implements ExternalDepositValidatePort {

    private final MockOpenBanking openBanking;

    @Override
    public boolean isDepositPossible(String bankCode, String accountNo, BigDecimal amount, String traceId) {
        log.info("외부 계좌 입금 가능 여부 조회 → {}, {}, {}, traceId = {}", bankCode, accountNo, amount,traceId);
        return openBanking.isAccountValid(bankCode, accountNo, amount);
    }
}
