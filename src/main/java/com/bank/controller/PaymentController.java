package com.bank.controller;

import com.bank.common.response.ApiResponse;
import com.bank.deposit.dto.ApprovalRequest;
import com.bank.deposit.dto.ApprovalResponse;
import com.bank.deposit.dto.EscrowReleaseRequest;
import com.bank.deposit.dto.EscrowReleaseResponse;
import com.bank.deposit.service.ApprovalService;
import com.bank.deposit.service.ConfirmReleaseService;
import com.bank.deposit.dto.AuthorizeRequest;
import com.bank.deposit.dto.AuthorizeResponse;
import com.bank.deposit.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final ApprovalService approveService;
    private final ConfirmReleaseService confirmReleaseService;

    @PostMapping("/authorize")
    public ApiResponse<AuthorizeResponse> authorize(@RequestBody AuthorizeRequest request) {
        /* [계정계] 결제 요청 로직
        * | 결제하기 전, 요청한 결제가 유효하고 정상적인 사용자의 요청인지 인증하는 단계 | */

        AuthorizeResponse response = paymentService.authorize(request);
        return ApiResponse.success(response);
    }
    @PostMapping("/approval")
    public ApiResponse<ApprovalResponse> approvePayment(@RequestBody ApprovalRequest request) {
        /* [계정계] 결제 승인 로직
         * | 실제 출금, 에스크로 계좌에 입금하는 로직 | */

        ApprovalResponse response = approveService.recodeLedger(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/confirm")
    public ApiResponse<EscrowReleaseResponse> confirmRelease(@Validated @RequestBody EscrowReleaseRequest request) {
        EscrowReleaseResponse response = confirmReleaseService.confirmRelease(request);
        return ApiResponse.success(response);
    }
}
