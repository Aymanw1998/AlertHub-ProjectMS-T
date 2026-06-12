package com.mst.scheduler;

import com.mst.dto.ActionKafkaDTO;
import com.mst.dto.ActionMapper;
import com.mst.model.Action;
import com.mst.model.RunOnDay;
import com.mst.repo.ActionRepo;
import com.mst.service.ActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@Slf4j
public class ActionJobScheduler {
    @Autowired
    private ActionRepo repo;
    @Autowired
    private ActionService service;
    @Autowired
    private ObjectMapper objectMapper;
    // 1800000 מילישניות = 30 דקות
    @Scheduled(fixedRate = 5000/*1800000*/)
    public void processScheduledActions() {
        log.info("Action Job Scheduler woke up! Fetching actions...");

        String currentDay = LocalDate.now().getDayOfWeek().name();
        RunOnDay dayEnum = RunOnDay.valueOf(currentDay); // המרה ל-Enum
        List<Action> actionsToRun = repo.findActiveActionsForToday(dayEnum);

        if (actionsToRun.isEmpty()) {
            log.info("No active actions found for today ({}). Going back to sleep.", currentDay);
            return;
        }

        List<String> infos = actionsToRun.stream()
                .map(ActionMapper::toDTO).toList().stream()
                .map(this::jsonString).filter(Objects::nonNull).toList();

        infos.forEach(service::publishMessage);
        log.info("Successfully pushed {} actions to Kafka.", infos.size());
    }
    private String jsonString(ActionKafkaDTO dto) {
        try{
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            return null;
        }
    }
}