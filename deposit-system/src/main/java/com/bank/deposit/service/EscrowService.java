package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.enums.HoldStatus;
import com.bank.deposit.domain.enums.MerchantId;
import com.bank.deposit.domain.enums.ReleaseType;
import com.bank.deposit.dto.AuthorizeRequest;
import com.bank.deposit.repository.AccountRepository;
import com.bank.deposit.repository.EscrowAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscrowService {

    private final EscrowAccountRepository escrowAccountRepository;
    private final AccountRepository accountRepository;
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.03"); // 3%
    private static final int SCALE = 2; // 소수점 이하 2자리
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP; // 반올림 방식



    @Transactional
    public String createEscrow(AuthorizeRequest request) {

        Account account = accountRepository
                .findByAccountId("WOORI_20")
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        //수수료, payment_amount 값 계산
        BigDecimal holdAmount = request.getAmount();
        BigDecimal platformFee = holdAmount
                .multiply(PLATFORM_FEE_RATE) // 내부 상수를 사용하여 계산
                .setScale(SCALE, ROUNDING_MODE);

        BigDecimal paymentAmount = holdAmount.subtract(platformFee);
        // 1. 필요한 비즈니스 값 계산 및 설정
        LocalDateTime now = LocalDateTime.now();

        // 에스크로 해제 예정일 설정 (거래일로부터 100일 후)
        LocalDate scheduledReleaseDate = LocalDate.now().plusDays(100);

        // 에스크로 만료일 설정 (거래일로부터 100일 후)
        LocalDate expiredDate = LocalDate.now().plusDays(100);
        // 고유 ID 생성 (UUID 또는 별도의 ID 생성 로직 사용)
        String newEscrowId = "ESC_" + UUID.randomUUID().toString().substring(0, 8);

        // 2. EscrowAccount 객체 생성 및 값 할당
        EscrowAccount escrowAccount = EscrowAccount.builder()
                // 필수 정보 및 계산된 정보
                .escrowAccountId(newEscrowId)
                .bankAccount(account)
                .holdAmount(holdAmount)
                .paymentAmount(paymentAmount)
                .platformFee(platformFee)
                .scheduledReleaseDate(scheduledReleaseDate)
                .expiredDate(expiredDate)

                // 지불인/수취인 정보 (실제로는 DTO 등으로 전달받아야 합니다)
                .payerBankCode(request.getPayerInfo().getBankCode())
                .payerAccount(request.getPayerInfo().getAccountNo())
                .payerName(request.getPayerInfo().getName())
                .payeeBankCode(request.getPayeeInfo().getBankCode())
                .payeeAccount(request.getPayeeInfo().getAccountNo())
                .payeeName(request.getPayeeInfo().getName())
                // 상태 및 일정 정보
                .holdStatus(HoldStatus.ACTIVE)
                .releaseType(ReleaseType.MANUAL)

                // 시간 및 주문 정보
                .holdStartDatetime(now)
                .merchantId(MerchantId.WORKET.getCode())
                .merchantOrderNo(request.getOrderNo())
                .build();

        // 3. Repository를 통해 DB에 저장
        // save() 메서드는 저장 후 엔티티를 반환합니다.
        EscrowAccount savedEscrow = escrowAccountRepository.save(escrowAccount);


        // 4. 저장된 escrowId 반환
        return savedEscrow.getEscrowAccountId();
    }
}