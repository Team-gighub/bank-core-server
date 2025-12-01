package com.bank.deposit.domain.enums;

public enum Description {
    에스크로계좌입금, 에스크로계좌출금, 사용자계좌출금, 사용자계좌입금,
    ESCROW_RELEASE_TO_PAYEE, ESCROW_RELEASE_FROM_PAYER,
    PLATFORM_FEE_COLLECTION,      // 에스크로에서 플랫폼 수수료 차감
    PLATFORM_FEE_RECEIVED         // 플랫폼 계좌로 수수료 입금
}
