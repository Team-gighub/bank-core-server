package com.bank.external.kftc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class MockCommonBanking {

    public boolean isWithdrawSuccess(String bankCode, String accountNo, BigDecimal amount) {
        boolean result = KftcRandomResponseGenerator.randomSuccess();
        log.info("[MOCK 공용금융망] 출금 성공 여부: {}", result);
        return result;
    }
}
