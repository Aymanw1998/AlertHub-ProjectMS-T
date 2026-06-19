package com.mst.service;

import com.mst.dto.LoggerRequestDTO;
import com.mst.model.Notification;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@Service
public class SmsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${logger.service.url}")
    private String loggerURL;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.messaging.service.sid}")
    private String fromSid;

    @KafkaListener(topics = "smsTopic", groupId = "sms-queue")
    public void listen(String message) {
        System.out.println("SmsMS received: " + message);

        try {
            Notification notification =
                    objectMapper.readValue(message, Notification.class);

            String sid = sendTwilioSMS(
                    notification.getTo(),
                    notification.getMessage()
            );

            System.out.println("SMS SENT SUCCESSFULLY to " + notification.getTo());
            System.out.println("Twilio SID: " + sid);

            sendLog(
                    "INFO",
                    "SMS sent successfully to " + notification.getTo()
                            + ". Twilio SID: " + sid
            );

        } catch (Exception e) {
            System.out.println("SMS SEND FAILED: " + e.getMessage());

            sendLog(
                    "ERROR",
                    "SMS sending failed: " + e.getMessage()
            );
        }
    }
    private void sendLog(String logLevel, String message) {
        try {
            LoggerRequestDTO dto = new LoggerRequestDTO();
            dto.setServiceName("SmsMS");
            dto.setLogLevel(logLevel);
            dto.setMessage(message);

            restTemplate.postForObject(loggerURL + "/create", dto, String.class);

        } catch (Exception e) {
            System.out.println("Failed to send log to LoggerMS: " + e.getMessage());
        }
    }

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    private String sendTwilioSMS(String to, String message) {

        Message twilioMessage = Message.creator(
                new PhoneNumber(formatIsraeliNumber(to)),
                fromSid,
                message
        ).create();

        return twilioMessage.getSid();
    }
    private String formatIsraeliNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        phone = phone.trim().replace(" ", "").replace("-", "");

        // 0501234567 -> +972501234567
        if (phone.startsWith("0")) {
            return "+972" + phone.substring(1);
        }

        // +9720501234567 -> +972501234567
        if (phone.startsWith("+9720")) {
            return "+972" + phone.substring(5);
        }

        // +972501234567
        if (phone.startsWith("+972")) {
            return phone;
        }

        throw new IllegalArgumentException("Invalid Israeli phone number: " + phone);
    }
}