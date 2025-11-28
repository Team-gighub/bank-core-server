package com.bank.common.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * [결제 요청] API (/payment/authorize) 응답 DTO
 */
@Builder
@Getter
public class PaymentRequestResponse {

    private String orderNo; // 워켓 거래 PK
    private String confirmToken; // 결제 승인에 사용할 토큰
}