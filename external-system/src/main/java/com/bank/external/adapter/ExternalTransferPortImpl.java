package com.bank.external.adapter;

import com.bank.common.port.ExternalTransferPort;
import org.springframework.stereotype.Component;

@Component
public class ExternalTransferPortImpl implements ExternalTransferPort {
    
    @Override
    public String send(String message) {
        System.out.println("External system sending: " + message);
        return "external-ok";
    }
} 
