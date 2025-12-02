package com.bank.common.port;

import java.math.BigDecimal;

public interface ExternalDepositValidatePort {

    /**
     * 외부 계좌 입금 가능 여부 확인
     *
     * @param bankCode 은행 코드
     * @param accountNo 계좌 번호
     * @param amount 입금 금액
     * @return 입금 가능 여부
     */
    boolean isDepositPossible(String bankCode, String accountNo, BigDecimal amount, String traceId);
}
