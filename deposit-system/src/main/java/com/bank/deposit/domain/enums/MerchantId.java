package com.bank.deposit.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MerchantId {

    WORKET("WK");

    private final String code;
}
