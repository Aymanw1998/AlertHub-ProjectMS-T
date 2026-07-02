package com.mst;

import com.mst.client.LoaderClient;
import com.mst.dto.DeveloperLabelCountResponse;
import com.mst.dto.LabelAggregateResponse;
import com.mst.dto.TaskAmountResponse;
import com.mst.model.Label;
import com.mst.model.Loader;
import com.mst.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = EvaluationService.class)
class EvaluationServiceTest {
    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private LoaderClient loaderClient;

    @Autowired
    private EvaluationService evaluationService;

    @Test
    void getDeveloperWithMostLabel_returnsDeveloperWithHighestCount() {
        when(loaderClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(
                loader("dev1", Label.BUG, 1),
                loader("dev1", Label.BUG, 2),
                loader("dev2", Label.BUG, 1),
                loader("dev2", Label.DOCUMENTATION, 1)
        )));

        DeveloperLabelCountResponse response =
                evaluationService.getDeveloperWithMostLabel("bug", 7, "test@test.com");

        assertEquals("dev1", response.getDeveloperId());
        assertEquals(Label.BUG, response.getLabel());
        assertEquals(2L, response.getTaskCount());
    }

    @Test
    void getLabelAggregate_countsLabelsForDeveloper() {
        when(loaderClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(
                loader("dev1", Label.BUG, 1),
                loader("dev1", Label.BUG, 2),
                loader("dev1", Label.DOCUMENTATION, 1),
                loader("dev2", Label.BUG, 1)
        )));

        LabelAggregateResponse response =
                evaluationService.getLabelAggregate("dev1", 7, "test@test.com");

        assertEquals(2L, response.getLabelCounts().get(Label.BUG));
        assertEquals(1L, response.getLabelCounts().get(Label.DOCUMENTATION));
    }

    @Test
    void getTaskAmount_countsTasksForDeveloperInsideTimeFrame() {
        when(loaderClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(
                loader("dev1", Label.BUG, 1),
                loader("dev1", Label.BUG, 2),
                loader("dev1", Label.BUG, 20),
                loader("dev2", Label.BUG, 1)
        )));

        TaskAmountResponse response =
                evaluationService.getTaskAmount("dev1", 7, "test");

        assertEquals("dev1", response.getDeveloperId());
        assertEquals(2L, response.getTaskAmount());
    }

    @Test
    void getTaskAmount_whenSinceInvalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> evaluationService.getTaskAmount("dev1", 0, "test@test.com"));
    }

    @Test
    void getLabelAggregate_whenDeveloperIdBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> evaluationService.getLabelAggregate(" ", 7, "test@test.com"));
    }

    private Loader loader(String developerId, Label label, int daysAgo) {
        Loader loader = new Loader();
        loader.setDeveloper_id(developerId);
        loader.setLabel(label);
        loader.setTimestamp(LocalDateTime.now().minusDays(daysAgo));
        return loader;
    }
}
