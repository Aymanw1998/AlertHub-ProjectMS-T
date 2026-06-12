package com.mst.controller;

import com.mst.model.Action;
import com.mst.scheduler.ActionJobScheduler;
import com.mst.service.ActionService;
import com.mst.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action")
public class ActionController {

    @Autowired
    private ActionService service;

    @Autowired
    private ActionJobScheduler scheduler;
    @GetMapping("/send/kafka")
    public ResponseEntity<String> sendData() {
        scheduler.processScheduledActions();
        return ResponseEntity.ok("successfully");
    }
    @GetMapping("/get-all")
    public ResponseEntity<List<Action>> getAllData() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/get-one/{id}")
    public ResponseEntity<?> getOneById(@PathVariable Long id) {
        try {
            Action action = service.getOneById(id);
            return ResponseEntity.ok(action);
        } catch (ActionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Action m) {
        try {
            Action createdAction = service.create(m);
            return ResponseEntity.ok(createdAction);
        } catch (InvalidNameException | InvalidActionTypeException |
                 InvalidMessageException | InvalidRecipientException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Action m) {
        try {
            Action updatedAction = service.update(id, m);
            return ResponseEntity.ok(updatedAction);
        } catch (ActionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidNameException | InvalidActionTypeException |
                 InvalidMessageException | InvalidRecipientException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (ActionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}