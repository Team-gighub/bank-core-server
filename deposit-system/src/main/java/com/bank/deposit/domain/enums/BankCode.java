package com.bank.deposit.domain.enums;

import lombok.Getter;

@Getter
public enum BankCode {
    OUR_BANK("020");

    private final String code;

    BankCode(String code) {
        this.code = code;
    }
}
