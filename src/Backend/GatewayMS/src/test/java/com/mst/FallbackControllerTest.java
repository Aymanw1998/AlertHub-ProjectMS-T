package com.mst;

import com.mst.controller.FallbackController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private FallbackController controller;

    @Test
    void fallback_returnsReadableMessageForService() {
        assertEquals("LOADER' service is DOWN {fallback}", controller.fallback("loader"));
    }
}
