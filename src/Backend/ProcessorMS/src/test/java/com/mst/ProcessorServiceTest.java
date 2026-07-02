package com.mst;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.mst.client.LoaderClient;
import com.mst.client.MetricClient;
import com.mst.dto.LoggerRequestDTO;
import com.mst.exceptions.InvalidConditionException;
import com.mst.model.Action;
import com.mst.model.ActionType;
import com.mst.model.Label;
import com.mst.model.Loader;
import com.mst.model.Metric;
import com.mst.service.ProcessorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.mst.model.Notification;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@SpringBootTest(
        classes = ProcessorService.class,
        properties = "logger.service.url=http://localhost:1016/api/logger"
)
class ProcessorServiceTest {

    @MockitoBean
    private LoaderClient loaderClient;

    @MockitoBean
    private MetricClient metricClient;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private ProcessorService processorService;

    @Test
    void processAction_whenConditionPasses_sendsNotificationToSmsTopic() throws Exception {
        Action action = action(ActionType.SMS);
        Metric metric = metric(1L, Label.BUG, 2, 24);

        when(loaderClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(
                loader(Label.BUG, 1),
                loader(Label.BUG, 2),
                loader(Label.DOCUMENTATION, 1)
        )));
        when(metricClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(metric)));
        when(objectMapper.readValue(eq("[[1]]"), any(TypeReference.class)))
                .thenReturn(List.of(List.of(1L)));
        when(objectMapper.writeValueAsString(any(Notification.class)))
                .thenReturn("{\"to\":\"+972508241000\",\"message\":\"Alert\"}");

        when(kafkaTemplate.send(eq("smsTopic"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        processorService.processAction(action);

        verify(kafkaTemplate).send(
                eq("smsTopic"),
                eq("{\"to\":\"+972508241000\",\"message\":\"Alert\"}")
        );
        verify(restTemplate).postForObject(
                eq("http://localhost:1016/api/logger/create"),
                org.mockito.ArgumentMatchers.argThat(body ->
                        body instanceof LoggerRequestDTO dto
                                && "ProcessorMS".equals(dto.getServiceName())
                                && "INFO".equals(dto.getLogLevel())
                                && dto.getMessage().contains("Kafka topic smsTopic")
                                && dto.getMessage().contains("smsTopic")
                                && dto.getMessage().contains("10")
                ),
                eq(String.class)
        );
    }

    @Test
    void processAction_whenConditionDoesNotPass_doesNotSendNotification() throws Exception {
        Action action = action(ActionType.EMAIL);
        Metric metric = metric(1L, Label.BUG, 3, 24);

        when(loaderClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(
                loader(Label.BUG, 1),
                loader(Label.DOCUMENTATION, 1)
        )));
        when(metricClient.getAllData()).thenReturn(ResponseEntity.ok(List.of(metric)));
        when(objectMapper.readValue(eq("[[1]]"), any(TypeReference.class)))
                .thenReturn(List.of(List.of(1L)));

        processorService.processAction(action);

        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void processAction_whenConditionMissing_throwsInvalidConditionException() {
        Action action = action(ActionType.SMS);
        action.setCondition(" ");

        assertThrows(InvalidConditionException.class, () -> processorService.processAction(action));
    }

    private Action action(ActionType type) {
        Action action = new Action();
        action.setId(10L);
        action.setOwner_id("1001");
        action.setName("Test Action");
        action.setAction_type(type);
        action.setCondition("[[1]]");
        action.setTo("+972508241000");
        action.setMessage("Alert");
        return action;
    }

    private Metric metric(Long id, Label label, int threshold, int hours) {
        Metric metric = new Metric();
        metric.setId(id);
        metric.setLabel(label);
        metric.setThreshold(threshold);
        metric.setTime_frame_hours(hours);
        return metric;
    }

    private Loader loader(Label label, int hoursAgo) {
        Loader loader = new Loader();
        loader.setLabel(label);
        loader.setTimestamp(LocalDateTime.now().minusHours(hoursAgo));
        return loader;
    }
}
