package com.bank.deposit.domain.enums;

// TODO: JPA Converter 적용
public enum Description {
    에스크로계좌입금,
    에스크로계좌출금,
    사용자계좌출금,
    사용자계좌입금,
    에스크로지급확정_에스크로출금_수취인,
    에스크로지급확정_수취인입금,
    에스크로지급확정_에스크로출금_수수료,      // 에스크로에서 플랫폼 수수료 차감
    에스크로지급확정_플랫폼수수료입금         // 플랫폼 계좌로 수수료 입금
}
