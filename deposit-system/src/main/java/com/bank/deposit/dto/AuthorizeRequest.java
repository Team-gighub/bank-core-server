package com.bank.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeRequest {
    private BigDecimal amount;
    private String orderNo;

    private PayeeInfoDto payeeInfo;

    private PayerInfoDto payerInfo;
}
