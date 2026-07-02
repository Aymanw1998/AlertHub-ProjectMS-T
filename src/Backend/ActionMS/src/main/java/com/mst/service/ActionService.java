package com.mst.service;

import com.mst.dto.ActionKafkaDTO;
import com.mst.dto.ActionMapper;
import com.mst.model.Action;
import com.mst.model.RunOnDay;
import com.mst.repo.ActionRepo;
import com.mst.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ActionService {
    @Autowired
    private ActionRepo repo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public int count = 0;
    public void publishMessage(String message) {
        kafkaTemplate.send("actionTopic",  message);
    }

    public Action triggerManually(Long id) throws ActionNotFoundException {
        Action action = getOneById(id);

        if (action.getIs_deleted()) {
            throw new ActionNotFoundException("Action with id:" +action.getId() + " not found");
        }

        ActionKafkaDTO dto = ActionMapper.toDTO(action);
        String json = jsonString(dto);

        if (json == null) {
            throw new RuntimeException("Failed to convert action to JSON");
        }

        publishMessage(json);

        action.setLast_run(new Timestamp(System.currentTimeMillis()));
        return repo.save(action);
    }

    @Scheduled(cron = "0 0/30 * * * *")
    public void scheduledActions() {

        String currentDay = LocalDate.now().getDayOfWeek().name();
        RunOnDay dayEnum = RunOnDay.valueOf(currentDay); // המרה ל-Enum
        LocalTime currentTime= LocalTime.now().withSecond(0).withNano(0);
        List<Action> actionsToRun = repo.findActiveActionsForToday(dayEnum, currentTime);

        if (actionsToRun.isEmpty()) {
            return;
        }

        List<Action> infos = actionsToRun.stream()
                            .filter(a-> a.getIs_enabled() == true && a.getIs_deleted() == false).toList();

        List<String> infosStr = infos.stream()
                                .map(ActionMapper::toDTO)
                                .map(this::jsonString)
                                .filter(Objects::nonNull)
                                .toList();

        infosStr.forEach(this::publishMessage);
        infos.forEach((a)-> a.setLast_run(new Timestamp(System.currentTimeMillis())));
        repo.saveAll(infos);
    }
    private String jsonString(ActionKafkaDTO dto) {
        try{
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            return null;
        }
    }
    public List<Action> getAll() {
        // מומלץ בהמשך לשנות את זה ל- repo.findByIsDeletedFalse() כדי לא להחזיר פעולות שנמחקו
        return repo.findAll();
    }

    public Action getOneById(Long id) throws ActionNotFoundException {
        return repo.findById(id)
                .orElseThrow(() -> new ActionNotFoundException("Action with id " + id + " not found!"));
    }

    public Action create(Action info) throws InvalidNameException, InvalidActionTypeException,
            InvalidMessageException, InvalidRecipientException, InvalidRunDayTimeException, InvalidConditionException {
        // הרצת וולידציות ידניות
        validateAction(info);

        // אתחול ערכי ברירת מחדל ליצירה
        info.setCreate_date(new Date());
        info.setIs_enabled(true);
        info.setIs_deleted(false);
        info.setLast_update(new Timestamp(System.currentTimeMillis()));

        return repo.save(info);
    }

    public Action update(Long id, Action info) throws ActionNotFoundException, InvalidNameException,
            InvalidActionTypeException, InvalidMessageException,
            InvalidRecipientException, InvalidRunDayTimeException, InvalidConditionException {
        // 1. וידוא קיום
        Action existingAction = getOneById(id);

        // 2. וולידציה על הנתונים החדשים
        validateAction(info);

        // 3. עדכון השדות
        existingAction.setName(info.getName());
        existingAction.setAction_type(info.getAction_type());
        existingAction.setRun_on_time(info.getRun_on_time());
        existingAction.setRun_on_day(info.getRun_on_day());
        existingAction.setMessage(info.getMessage());
        existingAction.setTo(info.getTo());
        existingAction.setIs_enabled(info.getIs_enabled());
        existingAction.setCondition(info.getCondition());

        // עדכון חותמת זמן של שינוי אחרון
        existingAction.setLast_update(new Timestamp(System.currentTimeMillis()));

        return repo.save(existingAction);
    }

    public void delete(Long id) throws ActionNotFoundException {
        Action action = getOneById(id);

        // מחיקה רכה (Soft Delete) לפי אפיון הפרויקט
        action.setIs_deleted(true);
        action.setIs_enabled(false);
        action.setLast_update(new Timestamp(System.currentTimeMillis()));

        repo.save(action);
    }

    public Action restore(Long id) throws ActionNotFoundException {
        Action action = getOneById(id);

        action.setIs_deleted(false);
        action.setIs_enabled(true);
        action.setLast_update(new Timestamp(System.currentTimeMillis()));

        return repo.save(action);
    }

    // מתודת עזר לוולידציה (הלוגיקה העסקית)
    private void validateAction(Action info) throws InvalidNameException, InvalidActionTypeException,
            InvalidMessageException, InvalidRecipientException, InvalidRunDayTimeException, InvalidConditionException {
        if (info.getName() == null || info.getName().trim().isBlank()) {
            throw new InvalidNameException("Action name cannot be empty");
        }

        if (info.getAction_type() == null) {
            throw new InvalidActionTypeException("Action type (SMS/EMAIL) is required");
        }

        if (info.getMessage() == null || info.getMessage().trim().isEmpty()) {
            throw new InvalidMessageException("Action message content cannot be empty");
        }

        if (info.getTo() == null || info.getTo().trim().isEmpty()) {
            throw new InvalidRecipientException("Recipient destination ('to' field) cannot be empty");
        }
        if (info.getRun_on_time() == null) {
            throw new InvalidRunDayTimeException("Run time is required");
        }

        int minute = info.getRun_on_time().getMinute();

        if (minute != 0 && minute != 30) {
            throw new InvalidRunDayTimeException("Run time must be full hour or half hour only");
        }
        if (info.getRun_on_time().getSecond() != 0) {
            throw new InvalidRunDayTimeException("Run time seconds must be 0");
        }

        if (info.getRun_on_day() == null) {
            throw new InvalidRunDayTimeException("Run day is required");
        }
        if (info.getCondition() == null || info.getCondition().isBlank()) {
            throw new InvalidConditionException("Condition is required");
        }
    }
}
