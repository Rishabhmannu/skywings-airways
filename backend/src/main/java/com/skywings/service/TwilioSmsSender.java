package com.skywings.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwilioSmsSender {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    private boolean initialized = false;

    private void ensureInitialized() {
        if (initialized) return;
        if ("placeholder".equals(accountSid) || accountSid == null || accountSid.isBlank()) {
            return;
        }
        try {
            Twilio.init(accountSid, authToken);
            initialized = true;
            log.info("Twilio SMS initialized successfully");
        } catch (Throwable e) {
            log.warn("Twilio initialization failed: {}. SMS OTP will be logged only.", e.getMessage());
        }
    }

    public void sendOtp(String toPhone, String otp) {
        ensureInitialized();

        if (!initialized) {
            log.info("Twilio not configured. OTP for {}: {}", toPhone, otp);
            throw new RuntimeException("Twilio SMS not configured");
        }

        Message.creator(
            new PhoneNumber(toPhone),
            new PhoneNumber(fromNumber),
            "Your SkyWings Airways verification code is: " + otp +
            ". Valid for 5 minutes. Do not share this code."
        ).create();

        log.info("SMS OTP sent to {}", toPhone);
    }
}
