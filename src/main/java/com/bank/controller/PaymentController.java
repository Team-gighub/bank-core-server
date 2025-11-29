package com.bank.controller;

import com.bank.common.response.ApiResponse;
import com.bank.deposit.dto.ApprovalRequest;
import com.bank.deposit.dto.ApprovalResponse;
import com.bank.deposit.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final ApprovalService approveService;

    @PostMapping("/approval")
    public ApiResponse<ApprovalResponse> approvePayment(@RequestBody ApprovalRequest request)
    {
        ApprovalResponse response = approveService.recodeLedger(request);
        return ApiResponse.success(response);
    }
}
