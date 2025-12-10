package com.bank.deposit.domain;

import com.bank.deposit.domain.enums.HoldStatus;
import com.bank.deposit.domain.enums.ReleaseType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_accounts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowAccount {

    @Id
    @Column(name = "escrow_account_id", length = 50, nullable = false)
    private String escrowAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    private Account bankAccount;

    @Column(name = "trace_id", length = 50)
    private String traceId;

    @Column(name = "hold_amount")
    private BigDecimal holdAmount;

    @Column(name = "payer_bank_code", length = 10)
    private String payerBankCode;

    @Column(name = "payer_account", length = 50)
    private String payerAccount;

    @Column(name = "payer_name", length = 100)
    private String payerName;

    @Column(name = "payee_bank_code", length = 10)
    private String payeeBankCode;

    @Column(name = "payee_account", length = 50)
    private String payeeAccount;

    @Column(name = "payee_name", length = 100)
    private String payeeName;

    @Column(name = "scheduled_release_date")
    private LocalDate scheduledReleaseDate;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "hold_status")
    private HoldStatus holdStatus = HoldStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_type")
    private ReleaseType releaseType = ReleaseType.MANUAL;

    @Column(name = "platform_fee", precision = 15, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "payment_amount", precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "hold_start_datetime")
    private LocalDateTime holdStartDatetime;

    @Column(name = "hold_end_datetime")
    private LocalDateTime holdEndDatetime;

    @Column(name = "merchant_id", length = 50)
    private String merchantId;

    @Column(name = "merchant_order_no", length = 50)
    private String merchantOrderNo;

    public void release() {
        this.holdAmount = BigDecimal.ZERO;
        this.holdStatus = HoldStatus.RELEASED;
        this.holdEndDatetime = LocalDateTime.now();
    }

}
