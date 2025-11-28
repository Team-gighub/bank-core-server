package com.bank.deposit.service;

import com.bank.deposit.dto.AuthorizeRequest;
import com.bank.deposit.dto.AuthorizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ValidateService validateService;

    public AuthorizeResponse authorize(AuthorizeRequest requestDto) {
        // 1. 계좌/잔액/타행 여부 검증
        validateService.validatePayer(requestDto.getPayerInfo(), requestDto.getAmount());

        // 2. 승인 토큰 발급 -----------------------------------------
        String approvalToken = validateService.generateApprovalToken();

        // 3. response 반환
        return new AuthorizeResponse(approvalToken);
    }

}
