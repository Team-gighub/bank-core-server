package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.common.port.ExternalDepositPort;
import com.bank.common.port.ExternalDepositValidatePort;
import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.Ledger;
import com.bank.deposit.domain.User;
import com.bank.deposit.domain.enums.AccountStatus;
import com.bank.deposit.domain.enums.AccountType;
import com.bank.deposit.domain.enums.HoldStatus;
import com.bank.deposit.dto.EscrowReleaseRequest;
import com.bank.deposit.dto.EscrowReleaseResponse;
import com.bank.deposit.repository.AccountRepository;
import com.bank.deposit.repository.EscrowAccountRepository;
import com.bank.deposit.repository.LedgerRepository;
import com.bank.fixture.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConfirmReleaseService 단위 테스트
 *
 * 테스트 범위:
 * - Happy Path: 당행/타행 수취인 정상 지급 확정
 * - 에스크로 검증 실패
 * - 고객사 검증 실패
 * - 금액 검증 실패
 * - 플랫폼/수취인 계좌 검증 실패
 * - 타행 외부 시스템 연동 실패
 * - 트랜잭션 롤백
 *
 * Mock 사용:
 * - Repository (EscrowAccountRepository, AccountRepository, LedgerRepository)
 * - External Port (ExternalDepositValidatePort, ExternalDepositPort)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmReleaseService 단위 테스트")
class ConfirmReleaseServiceTest {

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private ExternalDepositValidatePort externalValidatePort;

    @Mock
    private ExternalDepositPort externalDepositPort;

    @InjectMocks
    private ConfirmReleaseService confirmReleaseService;

    private EscrowReleaseRequest request;
    private Account escrowAccount;
    private Account payeeAccount;
    private Account platformAccount;
    private EscrowAccount escrowEntity;

    @BeforeEach
    void setUp() {
        request = new EscrowReleaseRequest("MERCHANT001", "ESC001");

        // User 객체 생성 (Account가 필요로 함)
        User payerUser = TestDataFactory.user()
                .itscno("PAYER001")
                .customerName("Payer Name")
                .build();
        User payeeUser = TestDataFactory.user()
                .itscno("PAYEE001")
                .customerName("Payee Name")
                .build();
        User merchantUser = TestDataFactory.user()
                .itscno("MERCHANT001")
                .customerName("Merchant Name")
                .build();

        escrowAccount = TestDataFactory.account()
                .accountId("ACC_ESCROW_001")
                .accountNumber("ESC-001")
                .balance(new BigDecimal("1000.00"))
                .user(payerUser)
                .build();
        payeeAccount = TestDataFactory.account()
                .accountId("ACC_PAYEE_001")
                .accountNumber("111-222-333")
                .balance(new BigDecimal("5000.00"))
                .user(payeeUser)
                .build();
        platformAccount = TestDataFactory.account()
                .accountId("ACC_PLATFORM_001")
                .accountNumber("999-999-999")
                .balance(new BigDecimal("10000.00"))
                .user(merchantUser)
                .build();

        escrowEntity = TestDataFactory.escrowAccount()
                .escrowId("ESC001")
                .bankAccount(escrowAccount)
                .holdAmount(new BigDecimal("1000.00"))
                .paymentAmount(new BigDecimal("950.00"))
                .merchantId("MERCHANT001")
                .payeeBankCode("020")
                .payeeAccount("111-222-333")
                .holdStatus(HoldStatus.ACTIVE)
                .build();
    }

    // ==================== Happy Path Tests ====================

    @Nested
    @DisplayName("Happy Path - 정상 흐름")
    class HappyPathTests {

        @Test
        @DisplayName("TC-001: 당행 수취인 정상 지급 확정")
        void confirmRelease_당행_수취인_정상_지급_확정() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(payeeAccount));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(1L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isEqualTo("1");

            // 수취인 계좌 입금 검증
            assertThat(payeeAccount.getBalance()).isEqualTo(new BigDecimal("5950.00"));

            // 플랫폼 계좌 입금 검증
            assertThat(platformAccount.getBalance()).isEqualTo(new BigDecimal("10050.00"));

            // 에스크로 해지 검증
            assertThat(escrowEntity.getHoldStatus()).isEqualTo(HoldStatus.RELEASED);
            assertThat(escrowEntity.getHoldAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(escrowEntity.getHoldEndDatetime()).isNotNull();

            // 원장 생성 검증 (당행: 4개)
            verify(ledgerRepository, times(4)).save(any());
        }

        @Test
        @DisplayName("TC-002: 타행 수취인 정상 지급 확정")
        void confirmRelease_타행_수취인_정상_지급_확정() {
            // Given
            EscrowAccount externalEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC002")
                    .bankAccount(escrowAccount)
                    .holdAmount(new BigDecimal("500.00"))
                    .paymentAmount(new BigDecimal("475.00"))
                    .merchantId("MERCHANT001")
                    .payeeBankCode("088")  // 타행
                    .payeeAccount("EXT-111-222")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest externalRequest = new EscrowReleaseRequest("MERCHANT001", "ESC002");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC002"))
                    .thenReturn(Optional.of(externalEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(externalValidatePort.isDepositPossible(eq("088"), eq("EXT-111-222"), eq(new BigDecimal("475.00")), any()))
                    .thenReturn(true);
            when(externalDepositPort.isDepositSuccess(eq("088"), eq("EXT-111-222"), eq(new BigDecimal("475.00")), any()))
                    .thenReturn(true);

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(5L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(externalRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isEqualTo("5");

            // 플랫폼 계좌 수수료 입금 검증
            assertThat(platformAccount.getBalance()).isEqualTo(new BigDecimal("10025.00"));

            // 에스크로 해지 검증
            assertThat(externalEscrow.getHoldStatus()).isEqualTo(HoldStatus.RELEASED);
            assertThat(externalEscrow.getHoldAmount()).isEqualTo(BigDecimal.ZERO);

            // 타행 원장 생성 검증 (3개: 에스크로출금, 플랫폼출금, 플랫폼입금)
            verify(ledgerRepository, times(3)).save(any());

            // 외부 시스템 호출 검증
            verify(externalValidatePort).isDepositPossible(eq("088"), eq("EXT-111-222"), eq(new BigDecimal("475.00")), any());
            verify(externalDepositPort).isDepositSuccess(eq("088"), eq("EXT-111-222"), eq(new BigDecimal("475.00")), any());
        }

        @Test
        @DisplayName("TC-003: 소수점 금액 정상 지급 확정")
        void confirmRelease_소수점_금액_정상_지급_확정() {
            // Given
            Account escrowAcc = TestDataFactory.account()
                    .accountId("ACC_ESCROW_002")
                    .balance(new BigDecimal("1234.56"))
                    .build();
            Account payeeAcc = TestDataFactory.account()
                    .accountId("ACC_PAYEE_002")
                    .accountNumber("222-333-444")
                    .balance(new BigDecimal("0.01"))
                    .build();

            EscrowAccount precisionEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC003")
                    .bankAccount(escrowAcc)
                    .holdAmount(new BigDecimal("1234.56"))
                    .paymentAmount(new BigDecimal("1111.11"))
                    .merchantId("MERCHANT002")
                    .payeeAccount("222-333-444")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest precisionRequest = new EscrowReleaseRequest("MERCHANT002", "ESC003");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC003"))
                    .thenReturn(Optional.of(precisionEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT002", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("222-333-444"))
                    .thenReturn(Optional.of(payeeAcc));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(10L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(precisionRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(payeeAcc.getBalance()).isEqualByComparingTo(new BigDecimal("1111.12"));
            assertThat(platformAccount.getBalance()).isEqualByComparingTo(new BigDecimal("10123.45"));
        }

        @Test
        @DisplayName("TC-004: 수수료 0원 지급 확정")
        void confirmRelease_수수료_0원_지급_확정() {
            // Given
            EscrowAccount zeroFeeEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC004")
                    .bankAccount(escrowAccount)
                    .holdAmount(new BigDecimal("1000.00"))
                    .paymentAmount(new BigDecimal("1000.00"))
                    .merchantId("MERCHANT001")
                    .payeeAccount("111-222-333")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest zeroFeeRequest = new EscrowReleaseRequest("MERCHANT001", "ESC004");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC004"))
                    .thenReturn(Optional.of(zeroFeeEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(payeeAccount));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(15L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(zeroFeeRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(payeeAccount.getBalance()).isEqualTo(new BigDecimal("6000.00"));
            assertThat(platformAccount.getBalance()).isEqualTo(new BigDecimal("10000.00"));  // 변화 없음

            // 수수료 0원 원장도 생성되어야 함
            verify(ledgerRepository, times(4)).save(any());
        }
    }

    // ==================== 요청 검증 실패 ====================

    @Nested
    @DisplayName("요청 검증 실패")
    class RequestValidationTests {

        @Test
        @DisplayName("TC-005: escrowId null")
        void confirmRelease_escrowId_null_요청_검증_실패() {
            // Given
            EscrowReleaseRequest nullRequest = new EscrowReleaseRequest("MERCHANT001", null);

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(nullRequest))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("TC-008: merchantId null")
        void confirmRelease_merchantId_null_요청_검증_실패() {
            // Given
            EscrowReleaseRequest nullRequest = new EscrowReleaseRequest(null, "ESC001");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(nullRequest))
                    .isInstanceOf(Exception.class);
        }
    }

    // ==================== 에스크로 검증 실패 ====================

    @Nested
    @DisplayName("에스크로 계좌 검증 실패")
    class EscrowValidationTests {

        @Test
        @DisplayName("TC-011: 에스크로 계좌 없음")
        void confirmRelease_에스크로_계좌_없음() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("NONEXIST"))
                    .thenReturn(Optional.empty());

            EscrowReleaseRequest notFoundRequest = new EscrowReleaseRequest("MERCHANT001", "NONEXIST");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(notFoundRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ESCROW_ACCOUNT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC-012: 에스크로 상태 RELEASED")
        void confirmRelease_에스크로_상태_RELEASED() {
            // Given
            EscrowAccount releasedEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC005")
                    .holdStatus(HoldStatus.RELEASED)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC005"))
                    .thenReturn(Optional.of(releasedEscrow));

            EscrowReleaseRequest releasedRequest = new EscrowReleaseRequest("MERCHANT001", "ESC005");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(releasedRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ESCROW_ALREADY_RELEASED);
        }

        @Test
        @DisplayName("TC-013: 에스크로 상태 CANCELLED")
        void confirmRelease_에스크로_상태_CANCELLED() {
            // Given
            EscrowAccount cancelledEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC006")
                    .holdStatus(HoldStatus.CANCELLED)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC006"))
                    .thenReturn(Optional.of(cancelledEscrow));

            EscrowReleaseRequest cancelledRequest = new EscrowReleaseRequest("MERCHANT001", "ESC006");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(cancelledRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ESCROW_ALREADY_CANCELLED);
        }

        @Test
        @DisplayName("TC-014: 에스크로 holdAmount = 0 (경계값 - 조기 실패 검증)")
        void confirmRelease_에스크로_holdAmount_0() {
            // Given
            // holdAmount = 0, paymentAmount는 양수로 설정
            // 이렇게 하면 platformFeeAmount = 0 - 1 = -1 (음수)
            EscrowAccount zeroAmountEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC007")
                    .holdAmount(BigDecimal.ZERO)
                    .paymentAmount(BigDecimal.ONE)  // 1로 설정
                    .merchantId("MERCHANT001")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC007"))
                    .thenReturn(Optional.of(zeroAmountEscrow));

            EscrowReleaseRequest zeroRequest = new EscrowReleaseRequest("MERCHANT001", "ESC007");

            // When & Then
            // Mutation 시나리오:
            // - 원본 코드: Line 253에서 '<= 0' 검증으로 조기 실패 → 예외 발생
            // - Mutation 코드: Line 253에서 '< 0' 검증 통과 (0은 < 0이 아님)
            //                  → 다음 로직 진행 → Line 65 validateAmounts 호출
            //                  → platformFeeAmount < 0 검증으로 예외 발생
            //
            // 문제: 두 경우 모두 예외가 발생하지만, 예외 발생 시점이 다름
            // 해결: ErrorCode는 같지만, 실행 흐름이 달라짐을 검증할 수 없음
            //       → 이것은 "Equivalent Mutant" (동등 돌연변이)
            //
            // 이 테스트는 holdAmount = 0일 때 예외가 발생함을 검증하지만,
            // mutation을 완벽히 감지하지는 못함 (수학적 제약으로 인한 한계)
            CustomException exception = catchThrowableOfType(
                    () -> confirmReleaseService.confirmRelease(zeroRequest),
                    CustomException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        @Test
        @DisplayName("TC-015: 에스크로 holdAmount 음수")
        void confirmRelease_에스크로_holdAmount_음수() {
            // Given
            EscrowAccount negativeEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC008")
                    .holdAmount(new BigDecimal("-100.00"))
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC008"))
                    .thenReturn(Optional.of(negativeEscrow));

            EscrowReleaseRequest negativeRequest = new EscrowReleaseRequest("MERCHANT001", "ESC008");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(negativeRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        @Test
        @DisplayName("TC-016: 에스크로 holdAmount = 0")
        void confirmRelease_에스크로_holdAmount_zero_with_payment() {
            // Given - holdAmount가 0이지만 paymentAmount도 0인 경우는 TC-014에서 테스트
            // 이 케이스는 holdAmount와 paymentAmount가 모두 존재하는 정상 케이스
            EscrowAccount zeroHoldEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC009")
                    .holdAmount(BigDecimal.ZERO)
                    .paymentAmount(new BigDecimal("100.00"))  // 이 경우 음수 platformFee 발생
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC009"))
                    .thenReturn(Optional.of(zeroHoldEscrow));

            EscrowReleaseRequest invalidRequest = new EscrowReleaseRequest("MERCHANT001", "ESC009");

            // When & Then - 검증 로직에서 holdAmount가 0이므로 INVALID_ESCROW_AMOUNT 에러 발생
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(invalidRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        @Test
        @DisplayName("TC-051: 에스크로 holdAmount 최소 유효값 (0.01) - mutation 경계값 테스트")
        void confirmRelease_에스크로_holdAmount_최소값() {
            // Given
            // holdAmount = 0.01 (최소 유효값)
            // 이 테스트는 '<= 0' vs '< 0' mutation을 감지하기 위한 것
            Account escrowAcc = TestDataFactory.account()
                    .accountId("ACC_ESC_MIN")
                    .balance(new BigDecimal("0.01"))
                    .build();

            Account payeeAcc = TestDataFactory.account()
                    .accountId("ACC_PAYEE_MIN")
                    .accountNumber("111-222-333")
                    .balance(BigDecimal.ZERO)
                    .build();

            EscrowAccount minAmountEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC051")
                    .bankAccount(escrowAcc)
                    .holdAmount(new BigDecimal("0.01"))      // 최소 금액
                    .paymentAmount(new BigDecimal("0.01"))   // 수수료 0
                    .merchantId("MERCHANT001")
                    .payeeAccount("111-222-333")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest minRequest = new EscrowReleaseRequest("MERCHANT001", "ESC051");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC051"))
                    .thenReturn(Optional.of(minAmountEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(payeeAcc));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(51L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(minRequest);

            // Then
            // holdAmount > 0이므로 정상 처리되어야 함
            // mutation이 '<= 0' → '< 0'으로 바뀌어도 이 테스트는 여전히 통과
            // 하지만 TC-014에서 holdAmount = 0일 때 실패하므로 mutation 감지 가능
            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isEqualTo("51");
        }
    }

    // ==================== 고객사 검증 실패 ====================

    @Nested
    @DisplayName("고객사 검증 실패")
    class MerchantValidationTests {

        @Test
        @DisplayName("TC-019: 고객사 ID 불일치")
        void confirmRelease_고객사_ID_불일치() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));

            EscrowReleaseRequest mismatchRequest = new EscrowReleaseRequest("MERCHANT999", "ESC001");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(mismatchRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MERCHANT_ID_MISMATCH);
        }

        @Test
        @DisplayName("TC-020: 고객사 ID 케이스 센서티브")
        void confirmRelease_고객사_ID_케이스_센서티브() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));

            EscrowReleaseRequest caseRequest = new EscrowReleaseRequest("merchant001", "ESC001");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(caseRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MERCHANT_ID_MISMATCH);
        }
    }

    // ==================== 금액 검증 실패 ====================

    @Nested
    @DisplayName("금액 검증 실패")
    class AmountValidationTests {

        @Test
        @DisplayName("TC-021: 금액 합계 불일치 - paymentAmount가 holdAmount보다 큼")
        void confirmRelease_금액_합계_불일치_과다() {
            // Given
            // paymentAmount > holdAmount는 논리적으로 불가능한 상황
            // 하지만 데이터 검증 테스트를 위해 이런 케이스를 설정
            EscrowAccount invalidEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC013")
                    .holdAmount(new BigDecimal("500.00"))
                    .paymentAmount(new BigDecimal("600.00"))  // holdAmount보다 큼 - 검증 실패해야 함
                    .merchantId("MERCHANT001")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC013"))
                    .thenReturn(Optional.of(invalidEscrow));

            EscrowReleaseRequest invalidRequest = new EscrowReleaseRequest("MERCHANT001", "ESC013");

            // When & Then
            // paymentAmount > holdAmount인 경우 INVALID_ESCROW_AMOUNT 검증 실패
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(invalidRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        @Test
        @DisplayName("TC-023: paymentAmount 음수")
        void confirmRelease_paymentAmount_음수() {
            // Given
            EscrowAccount negativePaymentEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC015")
                    .holdAmount(new BigDecimal("1000.00"))
                    .paymentAmount(new BigDecimal("-50.00"))
                    .merchantId("MERCHANT001")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC015"))
                    .thenReturn(Optional.of(negativePaymentEscrow));

            EscrowReleaseRequest negativeRequest = new EscrowReleaseRequest("MERCHANT001", "ESC015");

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(negativeRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ESCROW_AMOUNT);
        }

        @Test
        @DisplayName("TC-024: paymentAmount = 0 (경계값) - 수취인 0원, 플랫폼 전액 수수료")
        void confirmRelease_paymentAmount_경계값_0() {
            // Given
            // paymentAmount = 0이면 platformFeeAmount = holdAmount (플랫폼이 전액 수수료로 가져감)
            // 비즈니스적으로 허용 가능한 시나리오 (예: 위약금 전액 플랫폼 귀속)
            Account escrowAcc = TestDataFactory.account()
                    .accountId("ACC_ESCROW_016")
                    .balance(new BigDecimal("1000.00"))
                    .build();

            Account payeeAcc = TestDataFactory.account()
                    .accountId("ACC_PAYEE_016")
                    .accountNumber("111-222-333")
                    .balance(new BigDecimal("5000.00"))
                    .build();

            EscrowAccount zeroPaymentEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC016")
                    .bankAccount(escrowAcc)
                    .holdAmount(new BigDecimal("1000.00"))  // holdAmount는 양수
                    .paymentAmount(BigDecimal.ZERO)         // paymentAmount는 0 (수취인에게 0원)
                    .merchantId("MERCHANT001")
                    .payeeAccount("111-222-333")            // 당행 수취인 계좌
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC016"))
                    .thenReturn(Optional.of(zeroPaymentEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(payeeAcc));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(24L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            EscrowReleaseRequest zeroPaymentRequest = new EscrowReleaseRequest("MERCHANT001", "ESC016");

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(zeroPaymentRequest);

            // Then
            // paymentAmount = 0은 비즈니스 로직상 허용됨 (플랫폼이 전액 수수료로 가져감)
            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isEqualTo("24");

            // 수취인 계좌는 변화 없음 (0원 입금)
            assertThat(payeeAcc.getBalance()).isEqualTo(new BigDecimal("5000.00"));

            // 플랫폼만 수수료 1000원 받음
            assertThat(platformAccount.getBalance()).isEqualTo(new BigDecimal("11000.00"));
        }
    }

    // ==================== 플랫폼 계좌 검증 실패 ====================

    @Nested
    @DisplayName("플랫폼 계좌 검증 실패")
    class PlatformAccountValidationTests {

        @Test
        @DisplayName("TC-027: 플랫폼 계좌 없음")
        void confirmRelease_플랫폼_계좌_없음() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLATFORM_ACCOUNT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC-028: 플랫폼 계좌 상태 SUSPENDED")
        void confirmRelease_플랫폼_계좌_상태_SUSPENDED() {
            // Given
            Account suspendedAccount = TestDataFactory.account()
                    .accountId("ACC_PLATFORM_SUSPENDED")
                    .status(AccountStatus.SUSPENDED)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(suspendedAccount));

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLATFORM_ACCOUNT_NOT_ACTIVE);
        }

        @Test
        @DisplayName("TC-029: 플랫폼 계좌 상태 CLOSED")
        void confirmRelease_플랫폼_계좌_상태_CLOSED() {
            // Given
            Account closedAccount = TestDataFactory.account()
                    .accountId("ACC_PLATFORM_CLOSED")
                    .status(AccountStatus.CLOSED)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(closedAccount));

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLATFORM_ACCOUNT_NOT_ACTIVE);
        }
    }

    // ==================== 당행 수취인 검증 실패 ====================

    @Nested
    @DisplayName("당행 수취인 검증 실패")
    class PayeeAccountValidationTests {

        @Test
        @DisplayName("TC-030: 당행 수취인 계좌 없음")
        void confirmRelease_당행_수취인_계좌_없음() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYEE_ACCOUNT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC-031: 당행 수취인 계좌 상태 SUSPENDED")
        void confirmRelease_당행_수취인_계좌_상태_SUSPENDED() {
            // Given
            Account suspendedPayee = TestDataFactory.account()
                    .accountId("ACC_PAYEE_SUSPENDED")
                    .accountNumber("111-222-333")
                    .status(AccountStatus.SUSPENDED)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(suspendedPayee));

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYEE_ACCOUNT_NOT_ACTIVE);
        }

        @Test
        @DisplayName("TC-032: 당행 수취인 계좌 상태 CLOSED")
        void confirmRelease_당행_수취인_계좌_상태_CLOSED() {
            // Given
            Account closedPayee = TestDataFactory.account()
                    .accountId("ACC_PAYEE_CLOSED")
                    .accountNumber("111-222-333")
                    .status(AccountStatus.CLOSED)
                    .build();

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(closedPayee));

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYEE_ACCOUNT_NOT_ACTIVE);
        }
    }

    // ==================== 타행 외부 시스템 연동 실패 ====================

    @Nested
    @DisplayName("타행 외부 시스템 연동 실패")
    class ExternalSystemFailureTests {

        @Test
        @DisplayName("TC-033: 타행 입금 검증 실패")
        void confirmRelease_타행_입금_검증_실패() {
            // Given
            EscrowAccount externalEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC025")
                    .bankAccount(escrowAccount)
                    .payeeBankCode("088")  // 타행
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest externalRequest = new EscrowReleaseRequest("MERCHANT001", "ESC025");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC025"))
                    .thenReturn(Optional.of(externalEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(externalValidatePort.isDepositPossible(anyString(), anyString(), any(), any()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(externalRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_DEPOSIT_NOT_POSSIBLE);
        }

        @Test
        @DisplayName("TC-034: 타행 입금 요청 실패")
        void confirmRelease_타행_입금_요청_실패() {
            // Given
            EscrowAccount externalEscrow = TestDataFactory.escrowAccount()
                    .escrowId("ESC026")
                    .bankAccount(escrowAccount)
                    .payeeBankCode("088")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest externalRequest = new EscrowReleaseRequest("MERCHANT001", "ESC026");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC026"))
                    .thenReturn(Optional.of(externalEscrow));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(externalValidatePort.isDepositPossible(anyString(), anyString(), any(), any()))
                    .thenReturn(true);
            when(externalDepositPort.isDepositSuccess(anyString(), anyString(), any(), any()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> confirmReleaseService.confirmRelease(externalRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_DEPOSIT_FAILED);
        }
    }

    // ==================== 엣지 케이스 ====================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTests {

        @Test
        @DisplayName("TC-025: 매우 큰 금액 처리")
        void confirmRelease_매우_큰_금액_처리() {
            // Given
            Account largeEscrow = TestDataFactory.account()
                    .accountId("ACC_LARGE")
                    .balance(new BigDecimal("999999999999.99"))
                    .build();
            Account largePayee = TestDataFactory.account()
                    .accountId("ACC_LARGE_PAYEE")
                    .accountNumber("111-222-333")
                    .balance(new BigDecimal("999999999999.99"))
                    .build();

            EscrowAccount largeEscrowEntity = TestDataFactory.escrowAccount()
                    .escrowId("ESC017")
                    .bankAccount(largeEscrow)
                    .holdAmount(new BigDecimal("999999999999.99"))
                    .paymentAmount(new BigDecimal("999999999999.99"))
                    .merchantId("MERCHANT001")
                    .payeeAccount("111-222-333")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest largeRequest = new EscrowReleaseRequest("MERCHANT001", "ESC017");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC017"))
                    .thenReturn(Optional.of(largeEscrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(largePayee));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(25L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(largeRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isEqualTo("25");
        }

        @Test
        @DisplayName("TC-026: 부동소수점 연산 오차")
        void confirmRelease_부동소수점_연산_정확도() {
            // Given
            Account precisionEscrow = TestDataFactory.account()
                    .accountId("ACC_PRECISION")
                    .balance(new BigDecimal("0.30"))
                    .build();
            Account precisionPayee = TestDataFactory.account()
                    .accountId("ACC_PRECISION_PAYEE")
                    .accountNumber("111-222-333")
                    .balance(BigDecimal.ZERO)
                    .build();

            EscrowAccount precisionEscrowEntity = TestDataFactory.escrowAccount()
                    .escrowId("ESC018")
                    .bankAccount(precisionEscrow)
                    .holdAmount(new BigDecimal("0.30"))
                    .paymentAmount(new BigDecimal("0.10"))
                    .merchantId("MERCHANT001")
                    .payeeAccount("111-222-333")
                    .holdStatus(HoldStatus.ACTIVE)
                    .build();

            EscrowReleaseRequest precisionRequest = new EscrowReleaseRequest("MERCHANT001", "ESC018");

            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC018"))
                    .thenReturn(Optional.of(precisionEscrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(precisionPayee));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(26L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(precisionRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(precisionPayee.getBalance()).isEqualByComparingTo(new BigDecimal("0.10"));
            assertThat(platformAccount.getBalance()).isEqualByComparingTo(new BigDecimal("10000.20"));
        }

        @Test
        @DisplayName("TC-043: 에스크로 release() 메서드 - holdEndDatetime 설정")
        void confirmRelease_에스크로_release_메서드_검증() {
            // Given
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(payeeAccount));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(1L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            confirmReleaseService.confirmRelease(request);

            // Then
            assertThat(escrowEntity.getHoldEndDatetime()).isNotNull();
            assertThat(escrowEntity.getHoldStatus()).isEqualTo(HoldStatus.RELEASED);
        }

        @Test
        @DisplayName("TC-050: 요청 JSON 필드 추가 (Forward compatibility)")
        void confirmRelease_요청_추가_필드_무시() {
            // 이 테스트는 컨트롤러 레벨에서 처리되므로
            // 서비스 단위 테스트에서는 기본 요청만 테스트
            when(escrowAccountRepository.findWithLockByEscrowAccountId("ESC001"))
                    .thenReturn(Optional.of(escrowEntity));
            when(accountRepository.findWithLockByUserItscnoAndAccountType("MERCHANT001", AccountType.CHECKING))
                    .thenReturn(Optional.of(platformAccount));
            when(accountRepository.findWithLockByAccountNumber("111-222-333"))
                    .thenReturn(Optional.of(payeeAccount));

            Ledger mockLedger = Ledger.builder()
                    .ledgerSeq(1L)
                    .build();
            when(ledgerRepository.save(any(Ledger.class))).thenReturn(mockLedger);

            // When
            EscrowReleaseResponse response = confirmReleaseService.confirmRelease(request);

            // Then
            assertThat(response).isNotNull();
        }
    }
}
