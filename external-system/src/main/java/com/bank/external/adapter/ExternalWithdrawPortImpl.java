package com.bank.external.adapter;

import com.bank.common.port.ExternalWithdrawPort;
import com.bank.external.kftc.MockCommonBanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalWithdrawPortImpl implements ExternalWithdrawPort {
    private final MockCommonBanking commonBanking;
    @Override
    public boolean isWithdrawalSuccess(String bankCode, String accountNo, BigDecimal amount,String traceId) {
        log.info("외부 계좌 출금 성공 여부 조회 → {}, {}, {}, traceId = {}", bankCode, accountNo, amount,traceId);
        return commonBanking.isWithdrawSuccess(bankCode, accountNo, amount);

    }
}
