package com.mst.service;

import com.mst.model.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.mst.client.LoaderClient;
import com.mst.client.MetricClient;
import com.mst.dto.LoggerRequestDTO;
import com.mst.exceptions.ExternalServiceException;
import com.mst.exceptions.InvalidConditionException;
import com.mst.exceptions.MetricNotFoundException;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessorService {
    @Autowired
    private LoaderClient loaderClient;
    @Autowired
    private MetricClient metricClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${logger.service.url}")
    private String loggerURL;
    @Autowired
    private RestTemplate restTemplate;

    public ProcessorService(
            LoaderClient loaderClient,
            MetricClient metricClient,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.loaderClient = loaderClient;
        this.metricClient = metricClient;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "actionTopic", groupId = "job-queue")
    public void listen(String actionJson) {
        System.out.println("Processor received action: " + actionJson);

        try {
            Action action = objectMapper.readValue(actionJson, Action.class);
            processAction(action);

        } catch (FeignException e) {
            String msg = "External service error. Status: "
                    + e.status()
                    + ", Message: "
                    + e.getMessage();

            System.out.println(msg);
            sendLog("ERROR", msg);

        } catch (ExternalServiceException | InvalidConditionException | MetricNotFoundException e) {
            String msg = "Processor logic error: " + e.getMessage();

            System.out.println(msg);
            sendLog("WARN", msg);

        } catch (Exception e) {
            String msg = "Unexpected Processor error: " + e.getMessage();

            System.out.println(msg);
            sendLog("ERROR", msg);
        }
    }
    private void sendLog(String logLevel, String message) {
        try {
            LoggerRequestDTO dto = new LoggerRequestDTO();
            dto.setServiceName("ProcessorMS");
            dto.setLogLevel(logLevel);
            dto.setMessage(message);
            restTemplate.postForObject(loggerURL + "/create", dto, String.class);

        } catch (Exception e) {
            System.out.println("Failed to send log to LoggerMS: " + e.getMessage());
        }
    }

    public void processAction(Action action) {
        validateAction(action);

        List<Loader> loaders = getLoaders();
        List<Metric> metrics = getMetrics();

        boolean isConditionPassed = evaluateCondition(action.getCondition(), metrics, loaders);

        if (!isConditionPassed) {
            String msg = "Condition is false. Action id " + action.getId()
                    + " will not be sent to notification topic.";

            System.out.println(msg);
            sendLog("INFO", msg);

            return;
        }

        sendNotification(action);
    }

    private void validateAction(Action action) {
        if (action == null) {
            throw new InvalidConditionException("Action is null");
        }

        if (action.getCondition() == null || action.getCondition().isBlank()) {
            throw new InvalidConditionException("Action condition is empty");
        }

        if (action.getAction_type() == null) {
            throw new InvalidConditionException("Action type is null");
        }

        if (action.getTo() == null || action.getTo().isBlank()) {
            throw new InvalidConditionException("Action recipient is empty");
        }

        if (action.getMessage() == null || action.getMessage().isBlank()) {
            throw new InvalidConditionException("Action message is empty");
        }
    }

    private List<Loader> getLoaders() {
        ResponseEntity<List<Loader>> response = loaderClient.getAllData();

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ExternalServiceException("Failed to get data from LoaderMS");
        }

        return response.getBody();
    }

    private List<Metric> getMetrics() {
        ResponseEntity<List<Metric>> response = metricClient.getAllData();

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ExternalServiceException("Failed to get data from MetricMS");
        }

        return response.getBody();
    }

    private boolean evaluateCondition(String condition, List<Metric> metrics, List<Loader> loaders) {
        List<List<Long>> conditionGroups;

        try {
            conditionGroups = objectMapper.readValue(condition, new TypeReference<List<List<Long>>>() {});
        } catch (Exception e) {
            throw new InvalidConditionException("Invalid condition format: " + condition);
        }

        if (conditionGroups == null || conditionGroups.isEmpty()) {
            throw new InvalidConditionException("Condition groups are empty");
        }

        for (List<Long> group : conditionGroups) {
            boolean groupResult = true;

            for (Long metricId : group) {
                Metric metric = findMetricById(metrics, metricId);

                boolean metricResult = checkMetric(metric, loaders);

                if (!metricResult) {
                    sendLog(
                            "INFO",
                            "Metric failed. metricId="
                                    + metricId
                                    + ", action condition="
                                    + condition
                    );
                    groupResult = false;
                    break;
                }
            }

            if (groupResult) {
                return true;
            }
        }

        return false;
    }

    private Metric findMetricById(List<Metric> metrics, Long metricId) {
        return metrics.stream()
                .filter(metric -> metric.getId().equals(metricId))
                .findFirst()
                .orElseThrow(() -> new MetricNotFoundException("Metric with id " + metricId + " not found"));
    }

    private boolean checkMetric(Metric metric, List<Loader> loaders) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(metric.getTime_frame_hours());

        long count = loaders.stream()
                .filter(loader -> loader.getTimestamp() != null)
                .filter(loader -> loader.getTimestamp().isAfter(fromTime))
                .filter(loader -> loader.getLabel() != null)
                .filter(loader -> loader.getLabel().equals(metric.getLabel()))
                .count();

        System.out.println("Checking metric id= " + metric.getId() +", label= " + metric.getLabel() + ", count= " + count +", threshold= " + metric.getThreshold());

        return count >= metric.getThreshold();
    }

    private void sendNotification(Action action) {
        try {
            String topic = getTopicByActionType(action.getAction_type());

            Notification notification = new Notification();
            notification.setOwner_id(Long.parseLong(action.getOwner_id()));
            notification.setName(action.getName());
            notification.setTo(action.getTo());
            notification.setMessage(action.getMessage());

            String payload = objectMapper.writeValueAsString(notification);

            kafkaTemplate.send(topic, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    String errorMsg = "Failed to insert notification message into Kafka topic "
                            + topic
                            + " for action id "
                            + action.getId()
                            + ". Error: "
                            + ex.getMessage();

                    System.out.println(errorMsg);
                    sendLog("ERROR", errorMsg);

                } else {
                    String successMsg = "Processor inserted message into Kafka topic "
                            + topic
                            + " for action id "
                            + action.getId();

                    System.out.println(successMsg);
                    sendLog("INFO", successMsg);
                }
            });

        } catch (Exception e) {
            String msg = "Failed to build/send notification for action id "
                    + action.getId()
                    + ". Error: "
                    + e.getMessage();

            System.out.println(msg);
            sendLog("ERROR", msg);

            throw new RuntimeException(msg, e);
        }
    }
    private String getTopicByActionType(ActionType actionType) {
        return switch (actionType) {
            case EMAIL -> "emailTopic";
            case SMS -> "smsTopic";
        };
    }
}