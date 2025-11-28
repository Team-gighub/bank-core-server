package com.bank.deposit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class PayeeInfoDto {

    // 계좌 번호
    private String accountNo;

    // 은행 코드
    private String bankCode;

    // 예금주 명
    private String name;
}