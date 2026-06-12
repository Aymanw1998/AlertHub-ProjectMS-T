package com.mst.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mst.client.LoaderClient;
import com.mst.client.MetricClient;
import com.mst.exceptions.ExternalServiceException;
import com.mst.exceptions.InvalidConditionException;
import com.mst.exceptions.MetricNotFoundException;
import com.mst.model.Action;
import com.mst.model.Loader;
import com.mst.model.Metric;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ProcessorService {

    @Autowired
    private LoaderClient loaderClient;

    @Autowired
    private MetricClient metricClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // --- 1. מאזין הקפקא (לוכד את כל השגיאות!) ---
    @KafkaListener(topics = "actionTopic", groupId = "MS")
    public void listen(String info) {
        log.info("📥 Processor received action from Kafka: {}", info);

        try {
            processActionLogic(info);
            log.info("✅ Successfully processed action.");

        } catch (JsonProcessingException e) {
            log.error("❌ Invalid JSON format received from Kafka: {}", e.getMessage());
        } catch (FeignException e) {
            log.error("🔌 Network Error: Failed to fetch data from external microservice. Status: {}", e.status());
        } catch (ExternalServiceException e) {
            log.warn("⚠️ Logic/Data Error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("💥 CRITICAL: Unexpected error while processing action.", e);
        }
    }

    // --- 2. הלוגיקה המרכזית ---
    private void processActionLogic(String info) throws ExternalServiceException, JsonProcessingException, InvalidConditionException {
        Action action = objectMapper.readValue(info, Action.class);

        ResponseEntity<List<Loader>> loaderResponse = loaderClient.getAllData();
        ResponseEntity<List<Metric>> metricResponse = metricClient.getAllData();

        List<Loader> loaders = loaderResponse.getBody();
        List<Metric> metrics = metricResponse.getBody();

        if (loaders == null || metrics == null) {
            throw new ExternalServiceException("External services returned null data for loaders or metrics.");
        }

        boolean conditionResult = evaluateCondition(action.getCondition(), metrics, loaders);

        if (!conditionResult) {
            log.info("⏸️ Condition not passed for action id {}", action.getId());
            return;
        }

        sendNotification(action);
    }

    // --- 3. פענוח התנאים (עם זריקת שגיאות) ---
    private boolean evaluateCondition(String condition, List<Metric> metrics, List<Loader> loaders) throws InvalidConditionException {
        List<List<Long>> conditionGroups;

        try {
            conditionGroups = objectMapper.readValue(condition, new TypeReference<>() {});
        } catch (Exception e) {
            throw new InvalidConditionException("Cannot parse condition string to List<List<Long>>: " + condition);
        }

        return conditionGroups.stream()
                .anyMatch(group -> group.stream()
                        .allMatch(metricId -> metrics.stream()
                                .filter(m -> m.getId().equals(metricId))
                                .findFirst()
                                .map(metric -> checkMetric(metric, loaders))
                                // במקום להחזיר false ולהתעלם, אנחנו זורקים שגיאה ברורה:
                                .orElseThrow(() -> new MetricNotFoundException("Metric with id " + metricId + " not found in database!"))
                        )
                );
    }

    // --- 4. בדיקת המטריקה ---
    private boolean checkMetric(Metric metric, List<Loader> loaders) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(metric.getTime_frame_hours());

        long count = loaders.stream()
                .filter(loader -> loader.getTimestamp() != null)
                .filter(loader -> loader.getTimestamp().isAfter(fromTime))
                .filter(loader -> metric.getLabel() != null && metric.getLabel().equals(loader.getLabel()))
                .count();

        log.info("📊 Metric {} | label='{}' | count={} | threshold={}",
                metric.getId(), metric.getLabel(), count, metric.getThreshold());

        return count >= metric.getThreshold();
    }

    // --- 5. שליחת ההתראה הלאה ---
    private void sendNotification(Action action) {
        try {
            // הופכים את ה-Action בחזרה ל-JSON כדי שמיקרו-שירות ההתראות יוכל לקרוא אותו
            String payload = objectMapper.writeValueAsString(action);
            String topic;

            switch (action.getAction_type()) {
                case EMAIL -> topic = "emailTopic";
                case SMS -> topic = "smsTopic";
                default -> {
                    log.warn("Unknown action type: {}", action.getAction_type());
                    return;
                }
            }

            kafkaTemplate.send(topic, payload);
            log.info("🚀 Notification payload sent to {}: {}", topic, payload);

        } catch (Exception e) {
            log.error("Failed to parse action to JSON for notification", e);
        }
    }
}