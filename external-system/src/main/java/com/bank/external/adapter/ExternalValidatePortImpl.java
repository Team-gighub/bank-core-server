package com.bank.external.adapter;

import com.bank.common.port.ExternalValidatePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ExternalValidatePortImpl implements ExternalValidatePort {
    @Override
    public boolean isWithdrawalPossible(String bankCode, String accountNo, BigDecimal amount) {
        log.info("외부 계좌 출금 가능 여부 조회 → {}, {}, {}", bankCode, accountNo, amount);

        return true;

    }
}
