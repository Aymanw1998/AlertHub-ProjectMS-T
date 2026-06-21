package com.mst.controller;

import com.mst.dto.RoleResponseDTO;
import com.mst.exceptions.RoleNotFoundException;
import com.mst.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService service;

    @GetMapping("/get-all")
    public ResponseEntity<List<RoleResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
    @GetMapping("/get-one/{id}")
    public ResponseEntity<?> getOneById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getOneById(id));
        } catch (RoleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}
