package com.bank.fixture;

import com.bank.deposit.domain.*;
import com.bank.deposit.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 테스트 데이터 생성 팩토리
 * 테스트에서 필요한 엔티티 객체를 빠르게 생성
 */
public class TestDataFactory {

    // ==================== User ====================
    public static User createUser(String itscno, String customerName) {
        return User.builder()
                .itscno(itscno)
                .customerName(customerName)
                .build();
    }

    public static User createUser(String itscno) {
        return createUser(itscno, "Test User " + itscno);
    }

    // ==================== Account ====================
    public static Account createAccount(String accountId, User user, String accountNumber,
                                       BigDecimal balance, AccountStatus status) {
        return Account.builder()
                .accountId(accountId)
                .user(user)
                .accountNumber(accountNumber)
                .accountType(AccountType.CHECKING)
                .balance(balance)
                .status(status)
                .openDate(LocalDateTime.now())
                .version(1)
                .createdBy(CreatedBy.BANK_SYSTEM)
                .build();
    }

    public static Account createAccount(String accountId, String accountNumber, BigDecimal balance) {
        return createAccount(accountId, null, accountNumber, balance, AccountStatus.ACTIVE);
    }

    public static Account createAccount(String accountId, String accountNumber) {
        return createAccount(accountId, accountNumber, BigDecimal.ZERO);
    }

    public static Account createInactiveAccount(String accountId, String accountNumber, AccountStatus status) {
        return createAccount(accountId, null, accountNumber, BigDecimal.ZERO, status);
    }

    // ==================== EscrowAccount ====================
    public static EscrowAccount createEscrowAccount(
            String escrowId, Account bankAccount,
            BigDecimal holdAmount, BigDecimal paymentAmount,
            String merchantId, String payeeBankCode, String payeeAccount) {

        BigDecimal platformFee = holdAmount.subtract(paymentAmount);

        return EscrowAccount.builder()
                .escrowAccountId(escrowId)
                .bankAccount(bankAccount)
                .holdAmount(holdAmount)
                .paymentAmount(paymentAmount)
                .platformFee(platformFee)
                .payerBankCode(BankCode.OUR_BANK.getCode())
                .payerAccount("111-222-333")
                .payerName("Payer Name")
                .payeeBankCode(payeeBankCode)
                .payeeAccount(payeeAccount)
                .payeeName("Payee Name")
                .holdStatus(HoldStatus.ACTIVE)
                .releaseType(ReleaseType.MANUAL)
                .merchantId(merchantId)
                .merchantOrderNo("ORDER-" + escrowId)
                .holdStartDatetime(LocalDateTime.now())
                .build();
    }

    public static EscrowAccount createEscrowAccount(String escrowId, BigDecimal holdAmount,
                                                    BigDecimal paymentAmount, String merchantId,
                                                    String payeeBankCode, String payeeAccount) {
        return createEscrowAccount(escrowId, null, holdAmount, paymentAmount, merchantId, payeeBankCode, payeeAccount);
    }

    public static EscrowAccount createInactiveEscrowAccount(String escrowId, HoldStatus status) {
        return EscrowAccount.builder()
                .escrowAccountId(escrowId)
                .holdAmount(BigDecimal.ONE)
                .paymentAmount(BigDecimal.ZERO)
                .holdStatus(status)
                .merchantId("MERCHANT001")
                .build();
    }

    // ==================== Ledger ====================
    public static Ledger createLedger(
            String accountId, TransactionType transactionType,
            DebitCredit debitCredit, BigDecimal amount,
            BigDecimal balanceBefore, BigDecimal balanceAfter) {

        return Ledger.create(
                accountId,
                transactionType,
                debitCredit,
                amount,
                balanceBefore,
                balanceAfter,
                BankCode.OUR_BANK.getCode(),
                "counterparty-account",
                "Counterparty Name",
                Description.에스크로지급확정_에스크로출금_수취인
        );
    }

    // ==================== Builder Helper ====================
    public static class UserBuilder {
        private String itscno;
        private String customerName = "Test User";

        public UserBuilder itscno(String itscno) {
            this.itscno = itscno;
            return this;
        }

        public UserBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public User build() {
            return User.builder()
                    .itscno(itscno)
                    .customerName(customerName)
                    .build();
        }
    }

    public static class AccountBuilder {
        private String accountId;
        private User user;
        private String accountNumber;
        private BigDecimal balance = BigDecimal.ZERO;
        private AccountStatus status = AccountStatus.ACTIVE;
        private AccountType accountType = AccountType.CHECKING;

        public AccountBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public AccountBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AccountBuilder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public AccountBuilder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public AccountBuilder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        public Account build() {
            return Account.builder()
                    .accountId(accountId)
                    .user(user)
                    .accountNumber(accountNumber)
                    .balance(balance)
                    .status(status)
                    .accountType(accountType)
                    .openDate(LocalDateTime.now())
                    .version(1)
                    .createdBy(CreatedBy.BANK_SYSTEM)
                    .build();
        }
    }

    public static class EscrowAccountBuilder {
        private String escrowId;
        private Account bankAccount;
        private BigDecimal holdAmount = new BigDecimal("1000.00");
        private BigDecimal paymentAmount = new BigDecimal("950.00");
        private BigDecimal platformFee;
        private String merchantId = "MERCHANT001";
        private String payeeBankCode = BankCode.OUR_BANK.getCode();
        private String payeeAccount = "222-333-444";
        private String payeeName = "Payee Name";
        private String payerBankCode = BankCode.OUR_BANK.getCode();
        private String payerAccount = "111-222-333";
        private String payerName = "Payer Name";
        private HoldStatus holdStatus = HoldStatus.ACTIVE;
        private ReleaseType releaseType = ReleaseType.MANUAL;

        public EscrowAccountBuilder escrowId(String escrowId) {
            this.escrowId = escrowId;
            return this;
        }

        public EscrowAccountBuilder bankAccount(Account bankAccount) {
            this.bankAccount = bankAccount;
            return this;
        }

        public EscrowAccountBuilder holdAmount(BigDecimal holdAmount) {
            this.holdAmount = holdAmount;
            return this;
        }

        public EscrowAccountBuilder paymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
            return this;
        }

        public EscrowAccountBuilder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public EscrowAccountBuilder payeeBankCode(String payeeBankCode) {
            this.payeeBankCode = payeeBankCode;
            return this;
        }

        public EscrowAccountBuilder payeeAccount(String payeeAccount) {
            this.payeeAccount = payeeAccount;
            return this;
        }

        public EscrowAccountBuilder holdStatus(HoldStatus holdStatus) {
            this.holdStatus = holdStatus;
            return this;
        }

        public EscrowAccount build() {
            // holdAmount과 paymentAmount가 모두 존재할 때만 platformFee 계산
            if (holdAmount != null && paymentAmount != null) {
                this.platformFee = holdAmount.subtract(paymentAmount);
            } else {
                // null인 경우 platformFee도 null로 설정 (검증 테스트용)
                this.platformFee = null;
            }

            return EscrowAccount.builder()
                    .escrowAccountId(escrowId)
                    .bankAccount(bankAccount)
                    .holdAmount(holdAmount)
                    .paymentAmount(paymentAmount)
                    .platformFee(platformFee)
                    .merchantId(merchantId)
                    .payeeBankCode(payeeBankCode)
                    .payeeAccount(payeeAccount)
                    .payeeName(payeeName)
                    .payerBankCode(payerBankCode)
                    .payerAccount(payerAccount)
                    .payerName(payerName)
                    .holdStatus(holdStatus)
                    .releaseType(releaseType)
                    .merchantOrderNo("ORDER-" + escrowId)
                    .holdStartDatetime(LocalDateTime.now())
                    .build();
        }
    }

    // ==================== Builder Factory Methods ====================
    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static AccountBuilder account() {
        return new AccountBuilder();
    }

    public static EscrowAccountBuilder escrowAccount() {
        return new EscrowAccountBuilder();
    }
}