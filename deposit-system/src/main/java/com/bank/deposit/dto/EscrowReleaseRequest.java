package com.bank.deposit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EscrowReleaseRequest {

    @NotBlank(message = "고객사 아이디는 필수입니다")
    private String merchantId;

    @NotBlank(message = "에스크로 ID는 필수입니다")
    private String escrowId;

}
