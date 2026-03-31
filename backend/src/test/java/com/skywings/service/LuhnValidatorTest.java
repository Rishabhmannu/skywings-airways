package com.skywings.service;

import com.skywings.util.LuhnValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LuhnValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "4532015112830366",      // Valid Visa
            "5425233430109903",      // Valid Mastercard
            "4111111111111111",      // Common test Visa
            "4532 0151 1283 0366",   // With spaces
            "4532-0151-1283-0366",   // With dashes
    })
    void isValid_validCards_shouldReturnTrue(String cardNumber) {
        assertThat(LuhnValidator.isValid(cardNumber)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567890123456",      // Invalid Luhn
            "12345",                 // Too short
            "abcdefghijklmnop",      // Non-numeric
            "",                      // Empty
    })
    void isValid_invalidCards_shouldReturnFalse(String cardNumber) {
        assertThat(LuhnValidator.isValid(cardNumber)).isFalse();
    }

    @Test
    void getLastFour_shouldReturnLast4Digits() {
        assertThat(LuhnValidator.getLastFour("4532015112830366")).isEqualTo("0366");
        assertThat(LuhnValidator.getLastFour("4532 0151 1283 0366")).isEqualTo("0366");
    }
}
