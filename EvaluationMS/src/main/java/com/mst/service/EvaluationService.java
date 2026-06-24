package com.mst.service;

import com.mst.client.LoaderClient;
import com.mst.dto.DeveloperLabelCountResponse;
import com.mst.dto.LabelAggregateResponse;
import com.mst.dto.TaskAmountResponse;
import com.mst.model.Label;
import com.mst.model.PlatformInformation;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private LoaderClient loaderClient;

    public DeveloperLabelCountResponse getDeveloperWithMostLabel(String labelName, Integer sinceDays) {
        Label label = Label.fromString(labelName);
        LocalDateTime fromDate = getFromDate(sinceDays);
        List<PlatformInformation> data = getPlatformInformation();

        Map<String, Long> countsByDeveloper = data.stream()
                .filter(item -> isInTimeFrame(item, fromDate))
                .filter(item -> item.getLabel() == label)
                .filter(item -> hasText(item.getDeveloper_id()))
                .collect(Collectors.groupingBy(
                        PlatformInformation::getDeveloper_id,
                        Collectors.counting()
                ));

        return countsByDeveloper.entrySet().stream()
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
    }

    public LabelAggregateResponse getLabelAggregate(String developerId, Integer sinceDays) {
        validateDeveloperId(developerId);

        LocalDateTime fromDate = getFromDate(sinceDays);
        List<PlatformInformation> data = getPlatformInformation();

        Map<Label, Long> labelCounts = data.stream()
                .filter(item -> isInTimeFrame(item, fromDate))
                .filter(item -> developerId.equals(item.getDeveloper_id()))
                .filter(item -> item.getLabel() != null)
                .collect(Collectors.groupingBy(
                        PlatformInformation::getLabel,
                        Collectors.counting()
                ));

        return new LabelAggregateResponse(
                developerId,
                sinceDays,
                labelCounts
        );
    }

    public TaskAmountResponse getTaskAmount(String developerId, Integer sinceDays) {
        validateDeveloperId(developerId);

        LocalDateTime fromDate = getFromDate(sinceDays);
        List<PlatformInformation> data = getPlatformInformation();

        long taskAmount = data.stream()
                .filter(item -> isInTimeFrame(item, fromDate))
                .filter(item -> developerId.equals(item.getDeveloper_id()))
                .count();

        return new TaskAmountResponse(
                developerId,
                sinceDays,
                taskAmount
        );
    }

    private List<PlatformInformation> getPlatformInformation() {
        try {
            ResponseEntity<List<PlatformInformation>> response = loaderClient.getAllData();

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

    private boolean isInTimeFrame(PlatformInformation item, LocalDateTime fromDate) {
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
}