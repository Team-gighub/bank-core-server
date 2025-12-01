package com.bank.deposit.domain;

import com.bank.deposit.domain.enums.ChannelType;
import com.bank.deposit.domain.enums.DebitCredit;
import com.bank.deposit.domain.enums.Description;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "description")
    private Description description;

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

    //원장 생성 메서드
    public static Ledger create(
            String accountId,
            TransactionType transactionType, // 거래 유형
            DebitCredit debitCredit, // 차대구분
            BigDecimal amount, // 거래 금액
            BigDecimal balanceBefore, // 거래 전 잔액
            BigDecimal balanceAfter, // 거래 후 잔액
            String counterpartyBankCode,
            String counterpartyAccount,
            String counterpartyName,
            Description description // 적요
    ) {

        Ledger ledger = new Ledger();
        ledger.accountId = accountId;
        ledger.transactionType = transactionType;
        ledger.debitCredit = debitCredit;
        ledger.amount = amount;
        ledger.balanceBefore = balanceBefore;
        ledger.balanceAfter = balanceAfter;
        ledger.counterpartyBankCode = counterpartyBankCode;
        ledger.counterpartyAccount = counterpartyAccount;
        ledger.counterpartyName = counterpartyName;
        ledger.description = description;

        ledger.transactionDatetime = LocalDateTime.now();
        ledger.valueDate = LocalDate.now();
        ledger.channelType = ChannelType.BAAS;

        return ledger;
    }

}