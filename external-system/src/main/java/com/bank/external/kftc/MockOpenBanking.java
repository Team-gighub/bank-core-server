package com.bank.external.kftc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class MockOpenBanking  {

    public boolean isAccountValid(String bankCode, String accountNo, BigDecimal amount) {
        boolean result = KftcRandomResponseGenerator.randomSuccess();
        log.info("[MOCK 오픈뱅킹] 계좌 유효성: {}", result);
        return result;
    }
}
