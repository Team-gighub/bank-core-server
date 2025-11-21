package com.bank.external.adapter;

import com.bank.common.port.ExternalTransferPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalTransferPortImpl implements ExternalTransferPort {
    
    @Override
    public String send(String message) {
        log.info("External system sending: {}", message);
        return "external-ok";
    }
} 
