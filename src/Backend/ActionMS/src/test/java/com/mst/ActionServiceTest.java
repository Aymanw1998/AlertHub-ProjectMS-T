package com.mst;

import com.mst.exceptions.ActionNotFoundException;
import com.mst.exceptions.InvalidRunDayTimeException;
import com.mst.model.Action;
import com.mst.model.ActionType;
import com.mst.model.RunOnDay;
import com.mst.repo.ActionRepo;
import com.mst.service.ActionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ActionService.class)
class ActionServiceTest {

    @MockitoBean
    private ActionRepo actionRepo;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ActionService actionService;

    @Test
    void getAll_returnsActionsFromRepository() {
        Action action = validAction();
        when(actionRepo.findAll()).thenReturn(List.of(action));

        List<Action> result = actionService.getAll();

        assertEquals(1, result.size());
        assertEquals("Send SMS", result.get(0).getName());
        verify(actionRepo).findAll();
    }

    @Test
    void getOneById_whenMissing_throwsActionNotFoundException() {
        when(actionRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ActionNotFoundException.class, () -> actionService.getOneById(99L));
    }

    @Test
    void create_whenActionValid_setsDefaultsAndSaves() throws Exception {
        Action action = validAction();
        when(actionRepo.save(action)).thenReturn(action);

        Action saved = actionService.create(action);

        assertNotNull(saved.getCreate_date());
        assertNotNull(saved.getLast_update());
        assertTrue(saved.getIs_enabled());
        assertFalse(saved.getIs_deleted());
        verify(actionRepo).save(action);
    }

    @Test
    void create_whenRunTimeNotFullOrHalfHour_throwsInvalidRunDayTimeException() {
        Action action = validAction();
        action.setRun_on_time(LocalTime.of(10, 15));

        assertThrows(InvalidRunDayTimeException.class, () -> actionService.create(action));
    }

    @Test
    void update_whenActionExists_updatesFieldsAndLastUpdate() throws Exception {
        Action existing = validAction();
        existing.setId(1L);

        Action update = validAction();
        update.setName("Send Email");
        update.setAction_type(ActionType.EMAIL);
        update.setTo("manager@example.com");
        update.setMessage("Email alert");

        when(actionRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(actionRepo.save(existing)).thenReturn(existing);

        Action saved = actionService.update(1L, update);

        assertEquals("Send Email", saved.getName());
        assertEquals(ActionType.EMAIL, saved.getAction_type());
        assertEquals("manager@example.com", saved.getTo());
        assertNotNull(saved.getLast_update());
        verify(actionRepo).save(existing);
    }

    @Test
    void delete_whenActionExists_marksAsDeletedAndDisabled() throws Exception {
        Action existing = validAction();
        existing.setId(1L);
        when(actionRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(actionRepo.save(existing)).thenReturn(existing);

        actionService.delete(1L);

        assertTrue(existing.getIs_deleted());
        assertFalse(existing.getIs_enabled());
        assertNotNull(existing.getLast_update());
        verify(actionRepo).save(existing);
    }

    @Test
    void restore_whenActionExists_marksAsNotDeletedAndEnabled() throws Exception {
        Action existing = validAction();
        existing.setId(1L);
        existing.setIs_deleted(true);
        existing.setIs_enabled(false);
        when(actionRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(actionRepo.save(existing)).thenReturn(existing);

        Action restored = actionService.restore(1L);

        assertFalse(restored.getIs_deleted());
        assertTrue(restored.getIs_enabled());
        assertNotNull(restored.getLast_update());
        verify(actionRepo).save(existing);
    }

    private Action validAction() {
        Action action = new Action();
        action.setOwner_id("1");
        action.setName("Send SMS");
        action.setAction_type(ActionType.SMS);
        action.setRun_on_time(LocalTime.of(10, 30));
        action.setRun_on_day(RunOnDay.ALL);
        action.setMessage("Alert message");
        action.setTo("+972508241000");
        action.setIs_enabled(true);
        action.setIs_deleted(false);
        action.setCondition("[[1,2],[3]]");
        return action;
    }
}
