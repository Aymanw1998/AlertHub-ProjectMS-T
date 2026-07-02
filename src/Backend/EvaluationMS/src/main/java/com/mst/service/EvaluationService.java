package com.mst.service;

import com.mst.client.LoaderClient;
import com.mst.dto.DeveloperLabelCountResponse;
import com.mst.dto.LabelAggregateResponse;
import com.mst.dto.TaskAmountResponse;
import com.mst.model.Label;
import com.mst.model.Loader;
import com.mst.model.Notification;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private LoaderClient loaderClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public DeveloperLabelCountResponse getDeveloperWithMostLabel(String labelName, Integer sinceDays, String to) {

        Label label = Label.fromString(labelName);
        LocalDateTime fromDate = getFromDate(sinceDays);
        List<Loader> data = getPlatformInformation();

        Map<String, Long> countsByDeveloper = data.stream()
                .filter(item -> isInTimeFrame(item, fromDate))
                .filter(item -> item.getLabel() == label)
                .filter(item -> hasText(item.getDeveloper_id()))
                .collect(Collectors.groupingBy(
                        Loader::getDeveloper_id,
                        Collectors.counting()
                ));

        DeveloperLabelCountResponse response = countsByDeveloper.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> new DeveloperLabelCountResponse(
                        entry.getKey(),
                        label,
                        sinceDays,
                        entry.getValue()
                ))
                .orElse(new DeveloperLabelCountResponse(
                        null,
                        label,
                        sinceDays,
                        0L
                ));

        sendEvaluationResultToEmail(response, to);

        return response;
    }

    public LabelAggregateResponse getLabelAggregate(String developerId, Integer sinceDays, String to) {
        validateDeveloperId(developerId);

        LocalDateTime fromDate = getFromDate(sinceDays);
        List<Loader> data = getPlatformInformation();

        Map<Label, Long> labelCounts = data.stream()
                .filter(item -> isInTimeFrame(item, fromDate))
                .filter(item -> developerId.equals(item.getDeveloper_id()))
                .filter(item -> item.getLabel() != null)
                .collect(Collectors.groupingBy(
                        Loader::getLabel,
                        Collectors.counting()
                ));

        LabelAggregateResponse response = new LabelAggregateResponse(
                developerId,
                sinceDays,
                labelCounts
        );

        sendEvaluationResultToEmail(response, to);

        return response;
    }
    public TaskAmountResponse getTaskAmount(String developerId, Integer sinceDays, String to) {
        validateDeveloperId(developerId);

        LocalDateTime fromDate = getFromDate(sinceDays);
        List<Loader> data = getPlatformInformation();

        long taskAmount = data.stream()
                .filter(item -> isInTimeFrame(item, fromDate))
                .filter(item -> developerId.equals(item.getDeveloper_id()))
                .count();

        TaskAmountResponse response = new TaskAmountResponse(
                developerId,
                sinceDays,
                taskAmount
        );

        sendEvaluationResultToEmail(response, to);

        return response;
    }
    private List<Loader> getPlatformInformation() {
        try {
            ResponseEntity<List<Loader>> response = loaderClient.getAllData();

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Failed to get platform information from LoaderMS");
            }

            return response.getBody();

        } catch (FeignException e) {
            throw new RuntimeException("LoaderMS is unavailable. Status: " + e.status());
        }
    }

    private LocalDateTime getFromDate(Integer sinceDays) {
        if (sinceDays == null || sinceDays < 1) {
            throw new IllegalArgumentException("since must be greater than 0");
        }

        return LocalDateTime.now().minusDays(sinceDays);
    }

    private boolean isInTimeFrame(Loader item, LocalDateTime fromDate) {
        return item != null
                && item.getTimestamp() != null
                && item.getTimestamp().isAfter(fromDate);
    }

    private void validateDeveloperId(String developerId) {
        if (!hasText(developerId)) {
            throw new IllegalArgumentException("developerId is required");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void sendEvaluationResultToEmail(Object result, String to) {
        try {
            Notification notification = new Notification();
            notification.setName("EvaluationMS");
            notification.setTo(to);
            notification.setMessage(objectMapper.writeValueAsString(result));

            String json = objectMapper.writeValueAsString(notification);

            kafkaTemplate.send("emailTopic", json);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send evaluation result to emailTopic");
        }
    }
}