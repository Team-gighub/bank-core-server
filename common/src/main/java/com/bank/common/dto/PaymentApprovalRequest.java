package com.bank.common.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * [결제 승인] API (/payment/approval) 요청 DTO
 */
@Builder
@Getter
public class PaymentApprovalRequest {
    private String orderNo; //고객사 주문번호
    private BigDecimal amount; // 결제 금액
    private BasicAccountInfo payerInfo; // 구매자 (워켓의 경우 의뢰인) 정보
    private BasicAccountInfo payeeInfo; // 판매자 (워켓의 경우 프리랜서) 정보

}
