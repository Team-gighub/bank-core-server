package com.bank.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicAccountInfo {
    private String accountNo;
    private String bankCode;
    private String name;
}