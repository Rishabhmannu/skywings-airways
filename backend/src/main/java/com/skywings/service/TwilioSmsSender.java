package com.skywings.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        try {
            if (!"placeholder".equals(accountSid)) {
                Twilio.init(accountSid, authToken);
                initialized = true;
                log.info("Twilio SMS initialized successfully");
            } else {
                log.warn("Twilio credentials not configured. SMS OTP will be logged only.");
            }
        } catch (Exception e) {
            log.warn("Twilio initialization failed: {}. SMS OTP will be logged only.", e.getMessage());
        }
    }

    public void sendOtp(String toPhone, String otp) {
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
