package com.bank.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeRequest {
    private double amount;

    private PayeeInfoDto payeeInfo;

    private PayerInfoDto payerInfo;
}
