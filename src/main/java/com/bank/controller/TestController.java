package com.bank.controller;

import com.bank.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final DepositService depositService;

    @GetMapping("/test")
    public String test() {
        return depositService.test();
    }
}
