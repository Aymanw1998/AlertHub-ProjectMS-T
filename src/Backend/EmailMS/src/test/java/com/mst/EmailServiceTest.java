package com.mst;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mst.dto.LoggerRequestDTO;
import com.mst.model.Notification;
import com.mst.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = EmailService.class,
        properties = "logger.service.url=http://localhost:1016/api/logger"
)
class EmailServiceTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @Test
    void listen_whenMessageValid_sendsEmailAndInfoLog() throws Exception {
        Notification notification = new Notification();
        notification.setTo("manager@example.com");
        notification.setMessage("Alert message");

        when(objectMapper.readValue("email-json", Notification.class))
                .thenReturn(notification);

        emailService.listen("email-json");

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(mailCaptor.capture());
        SimpleMailMessage mail = mailCaptor.getValue();

        assertArrayEquals(new String[]{"manager@example.com"}, mail.getTo());
        assertEquals("Alert Hub Notification", mail.getSubject());
        assertEquals("Alert message", mail.getText());

        verify(restTemplate).postForObject(
                eq("http://localhost:1016/api/logger/create"),
                org.mockito.ArgumentMatchers.argThat(body ->
                        body instanceof LoggerRequestDTO dto
                                && "EmailMS".equals(dto.getServiceName())
                                && "INFO".equals(dto.getLogLevel())
                                && dto.getMessage().contains("Email sent successfully")
                ),
                eq(String.class)
        );
    }

    @Test
    void listen_whenMailSenderFails_sendsErrorLog() throws Exception {
        Notification notification = new Notification();
        notification.setTo("manager@example.com");
        notification.setMessage("Alert message");

        when(objectMapper.readValue("email-json", Notification.class))
                .thenReturn(notification);
        doThrow(new RuntimeException("SMTP failed"))
                .when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        emailService.listen("email-json");

        verify(restTemplate).postForObject(
                eq("http://localhost:1016/api/logger/create"),
                org.mockito.ArgumentMatchers.argThat(body ->
                        body instanceof LoggerRequestDTO dto
                                && "EmailMS".equals(dto.getServiceName())
                                && "ERROR".equals(dto.getLogLevel())
                                && dto.getMessage().contains("SMTP failed")
                ),
                eq(String.class)
        );
    }
}
