package com.skywings.util;

public final class LuhnValidator {

    private LuhnValidator() {}

    public static boolean isValid(String cardNumber) {
        String sanitized = cardNumber.replaceAll("[\\s-]", "");

        if (!sanitized.matches("\\d{13,19}")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = sanitized.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(sanitized.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    public static String getLastFour(String cardNumber) {
        String sanitized = cardNumber.replaceAll("[\\s-]", "");
        return sanitized.substring(sanitized.length() - 4);
    }
}
