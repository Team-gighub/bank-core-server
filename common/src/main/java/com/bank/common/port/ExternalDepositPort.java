package com.bank.common.port;

import java.math.BigDecimal;

public interface ExternalDepositPort {

    /**
     * 외부 계좌 입금 요청
     *
     * @param bankCode 은행 코드
     * @param accountNo 계좌 번호
     * @param amount 입금 금액
     * @return 입금 성공 여부
     */
    boolean isDepositSuccess(String bankCode, String accountNo, BigDecimal amount, String traceId);
}
