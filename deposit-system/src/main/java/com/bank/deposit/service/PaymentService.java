package com.bank.deposit.service;

import com.bank.deposit.dto.AuthorizeRequest;
import com.bank.deposit.dto.AuthorizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ValidateService validateService;
    private final ApprovalTokenService approvalTokenService;
    private final EscrowService escrowService;

    public AuthorizeResponse authorize(AuthorizeRequest request) {
        // 1. 계좌/잔액/타행 여부 검증
        validateService.validatePayer(request.getPayerInfo(), request.getAmount());

        // 2. 승인 토큰 발급
        String approvalToken = approvalTokenService.generate();

        // 3. escrow 테이블 등록
        String escrowId = escrowService.createEscrow(request);

        // 4. response 반환
        AuthorizeResponse response = new AuthorizeResponse(request.getOrderNo(), approvalToken, escrowId);
        return response;
    }

}
