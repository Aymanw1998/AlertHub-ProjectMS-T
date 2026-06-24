package com.mst;

import com.mst.model.Logger;
import com.mst.repo.LoggerRepo;
import com.mst.service.LoggerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = LoggerService.class)
class LoggerServiceTest {

    @MockitoBean
    private LoggerRepo loggerRepo;

    @Autowired
    private LoggerService loggerService;

    @Test
    void getAllData_returnsAllLogsFromRepository() {
        Logger log = new Logger();
        log.setServiceName("EmailMS");

        when(loggerRepo.findAll()).thenReturn(List.of(log));

        List<Logger> result = loggerService.getAllData();

        assertEquals(1, result.size());
        assertEquals("EmailMS", result.get(0).getServiceName());
        verify(loggerRepo).findAll();
    }

    @Test
    void create_setsTimestampAndSavesLog() {
        Logger log = new Logger();
        log.setServiceName("SmsMS");
        log.setLogLevel("INFO");
        log.setMessage("SMS sent");

        when(loggerRepo.save(log)).thenReturn(log);

        Logger saved = loggerService.create(log);

        assertNotNull(saved.getTimestamp());
        assertEquals("SmsMS", saved.getServiceName());
        verify(loggerRepo).save(log);
    }
}
