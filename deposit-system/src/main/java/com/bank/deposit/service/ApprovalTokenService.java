package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ApprovalTokenService {

    private static final int TOKEN_VALID_MINUTES = 3;

    public String generate() {
        String uuid = UUID.randomUUID().toString();
        long expireAt = Instant.now()
                .plus(TOKEN_VALID_MINUTES, ChronoUnit.MINUTES)
                .toEpochMilli();

        return uuid + "." + expireAt;
    }

    public boolean isValid(String token) {
        try {
            String[] split = token.split("\\.");
            if (split.length != 2) return false;

            long expireTime = Long.parseLong(split[1]);
            long now = Instant.now().toEpochMilli();

            return now < expireTime;

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
