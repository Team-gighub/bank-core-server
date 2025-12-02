package com.bank.external.adapter;

import com.bank.common.port.ExternalDepositPort;
import com.bank.external.kftc.MockCommonBanking;
import com.bank.external.kftc.MockOpenBanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalDepositPortImpl implements ExternalDepositPort {

    private final MockCommonBanking commonBanking;

    @Override
    public boolean isDepositSuccess(String bankCode, String accountNo, BigDecimal amount, String traceId) {
        log.info("외부 계좌 출금 요청 진입 → {}, {}, {}, traceId = {}", bankCode, accountNo, amount,traceId);
        return commonBanking.isDepositSuccess(bankCode, accountNo, amount);
    }
}
