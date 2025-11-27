package com.bank.deposit.domain;

import com.bank.deposit.domain.enums.ChannelType;
import com.bank.deposit.domain.enums.DebitCredit;
import com.bank.deposit.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_seq", nullable = false)
    private Long ledgerSeq;

    @Column(name = "account_id", length = 50)
    private String accountId;

    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "debit_credit")
    private DebitCredit debitCredit;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_bank_code", length = 10)
    private String counterpartyBankCode;

    @Column(name = "counterparty_account", length = 50)
    private String counterpartyAccount;

    @Column(name = "counterparty_name", length = 100)
    private String counterpartyName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "transaction_datetime")
    private LocalDateTime transactionDatetime;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Column(name = "is_cancelled")
    private Boolean isCancelled = false;

    @Column(name = "cancelled_by_ledger_seq")
    private Long cancelledByLedgerSeq;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    private ChannelType channelType = ChannelType.BAAS;

}