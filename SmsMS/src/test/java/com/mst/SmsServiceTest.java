package com.mst;

import com.mst.dto.LoggerRequestDTO;
import com.mst.model.Notification;
import com.mst.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = SmsService.class,
        properties = {
                "logger.service.url=http://localhost:1016/api/logger",
                "twilio.messaging.service.sid=MG123"
        }
)
class SmsServiceTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Autowired
    private SmsService smsService;

    @Test
    void formatIsraeliNumber_whenStartsWithZero_returnsInternationalNumber() throws Exception {
        assertEquals("+972508241000", formatIsraeliNumber("050-824 1000"));
    }

    @Test
    void formatIsraeliNumber_whenStartsWithPlus972Zero_removesExtraZero() throws Exception {
        assertEquals("+972508241000", formatIsraeliNumber("+9720508241000"));
    }

    @Test
    void formatIsraeliNumber_whenInvalid_throwsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatIsraeliNumber("12345")
        );

        assertEquals("Invalid Israeli phone number: 12345", exception.getMessage());
    }

    @Test
    void listen_whenPhoneInvalid_sendsErrorLogToLoggerService() throws Exception {
        Notification notification = new Notification();
        notification.setTo("12345");
        notification.setMessage("Hello");

        when(objectMapper.readValue("bad-phone-json", Notification.class))
                .thenReturn(notification);

        smsService.listen("bad-phone-json");

        verify(restTemplate).postForObject(
                eq("http://localhost:1016/api/logger/create"),
                org.mockito.ArgumentMatchers.argThat(body ->
                        body instanceof LoggerRequestDTO dto
                                && "SmsMS".equals(dto.getServiceName())
                                && "ERROR".equals(dto.getLogLevel())
                                && dto.getMessage().contains("Invalid Israeli phone number")
                ),
                eq(String.class)
        );
    }

    private String formatIsraeliNumber(String phone) throws Exception {
        Method method = SmsService.class.getDeclaredMethod("formatIsraeliNumber", String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(smsService, phone);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }
}
