package com.mst.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.api.key}")
    private String apiKey;

    @Value("${twilio.api.secret}")
    private String apiSecret;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @PostConstruct
    public void init() {
        Twilio.init(apiKey, apiSecret, accountSid);    }
}
