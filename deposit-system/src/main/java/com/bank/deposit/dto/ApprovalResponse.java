package com.bank.deposit.dto;

import com.bank.deposit.domain.enums.HoldStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {
    private String escrowId;
    private BigDecimal holdAmount;
    private HoldStatus holdStatus;
    private BigDecimal platformFee;
    private LocalDateTime  holdStartDatetime;
}
