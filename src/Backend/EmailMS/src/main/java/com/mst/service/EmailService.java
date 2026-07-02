package com.mst.service;

import tools.jackson.databind.ObjectMapper;
import com.mst.dto.LoggerRequestDTO;
import com.mst.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${logger.service.url}")
    private String loggerURL;

    @KafkaListener(topics = "emailTopic", groupId = "email-queue")
    public void listen(String message) {
        System.out.println("EmailMS received: " + message);

        try {
            Notification notification =
                    objectMapper.readValue(message, Notification.class);

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(notification.getTo());
            mail.setSubject("Alert Hub Notification");
            mail.setText(notification.getMessage());

            mailSender.send(mail);

            System.out.println("EMAIL SENT SUCCESSFULLY to " + notification.getTo());

            sendLog(
                    "INFO",
                    "Email sent successfully to " + notification.getTo()
                            + " and the message is:  " + notification.getMessage()
            );

        } catch (Exception e) {
            System.out.println("EMAIL SEND FAILED: " + e.getMessage());

            sendLog(
                    "ERROR",
                    "Email sending failed: " + e.getMessage()
            );
        }
    }

    private void sendLog(String logLevel, String message) {
        try {
            LoggerRequestDTO dto = new LoggerRequestDTO();
            dto.setServiceName("EmailMS");
            dto.setLogLevel(logLevel);
            dto.setMessage(message);

            restTemplate.postForObject(loggerURL + "/create", dto, String.class);

        } catch (Exception e) {
            System.out.println("Failed to send log to LoggerMS: " + e.getMessage());
        }
    }
}