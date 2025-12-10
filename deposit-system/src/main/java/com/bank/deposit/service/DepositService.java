package com.bank.deposit.service;

import com.bank.common.port.ExternalTransferPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositService {
    private final ExternalTransferPort externalTransferPort;

    public String test() {
        return externalTransferPort.send("hello");
    }
}
