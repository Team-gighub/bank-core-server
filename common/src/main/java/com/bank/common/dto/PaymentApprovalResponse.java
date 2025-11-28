package com.bank.common.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * [결제 승인] API (/payment/approval) 응답 DTO
 */
@Getter
@Builder
public class PaymentApprovalResponse {
    private String escrowId; // 에스크로 결제 승인 TID
}
