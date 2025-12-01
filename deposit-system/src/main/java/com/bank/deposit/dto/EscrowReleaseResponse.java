package com.bank.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EscrowReleaseResponse {

    private String paymentId;

    /**
     * 원장 PK로부터 응답 생성
     */
    public static EscrowReleaseResponse fromLedgerSeq(Long ledgerSeq) {
        return new EscrowReleaseResponse(String.valueOf(ledgerSeq));
    }
}
