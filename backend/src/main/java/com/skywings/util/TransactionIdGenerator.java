package com.skywings.util;

import java.security.SecureRandom;

public final class TransactionIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HEX_CHARS = "0123456789ABCDEF";

    private TransactionIdGenerator() {}

    public static String generate() {
        return "SKY-" + randomHex(4) + "-" + randomHex(4);
    }

    private static String randomHex(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(HEX_CHARS.charAt(RANDOM.nextInt(HEX_CHARS.length())));
        }
        return sb.toString();
    }
}
