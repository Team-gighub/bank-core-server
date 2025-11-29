package com.bank.common.port;

import java.math.BigDecimal;

public interface ExternalValidatePort {

    /**
     * 외부 계좌 출금 가능 여부 확인
     *
     * @param bankCode 은행 코드
     * @param accountNo 계좌 번호
     * @param amount 출금 금액
     * @return 출금 가능 여부
     */
    boolean isWithdrawalPossible(String bankCode, String accountNo, BigDecimal amount);

}
