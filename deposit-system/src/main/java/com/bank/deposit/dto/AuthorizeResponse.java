package com.bank.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizeResponse {
    /**
     * 결제 요청 검증 후 발급되는 승인 토큰
     */
    private String approvalToken;
}
