package com.bank.external.kftc;

import java.util.Random;

public class KftcRandomResponseGenerator {

    private KftcRandomResponseGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Random RANDOM = new Random();

    /**
     * 1% 확률로 false, 99% 확률로 true 반환
     */
    public static boolean randomSuccess() {
        int number = RANDOM.nextInt(100); // 0~99
        return number != 0; // 0일 때만 실패
    }
}