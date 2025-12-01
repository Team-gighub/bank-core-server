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

    @NotBlank(message = "요청일자는 필수입니다")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "요청일자 형식은 YYYY-MM-DD 입니다")
    private String reqYmd;

    @NotBlank(message = "에스크로 ID는 필수입니다")
    private String escrowId;

    @NotBlank(message = "변경자 ID는 필수입니다")
    private String changerId;

}
