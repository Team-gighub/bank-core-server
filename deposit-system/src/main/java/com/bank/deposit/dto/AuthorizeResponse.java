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

    private String orderNo;
    private String approvalToken;
    private String escrowId;
}
