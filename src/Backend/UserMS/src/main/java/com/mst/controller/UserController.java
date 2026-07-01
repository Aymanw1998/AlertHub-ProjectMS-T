package com.mst.controller;

import com.mst.dto.UserMapper;
import com.mst.dto.UserRequestDTO;
import com.mst.dto.UserResponseDTO;
import com.mst.dto.UserSecurityResponseDTO;
import com.mst.exceptions.InvalidUserException;
import com.mst.exceptions.UserAlreadyExistsException;
import com.mst.exceptions.UserNotFoundException;
import com.mst.model.User;
import com.mst.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping("/get-all")
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        List<User> users = service.getAll();
        List<UserResponseDTO> dto = users.stream().map(UserMapper::toDTO).toList();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-one/{id}")
    public ResponseEntity<?> getOneById(@PathVariable Long id) {
        try {
            User user = service.getOneById(id);
            UserResponseDTO dto = UserMapper.toDTO(user);
            return ResponseEntity.ok(dto);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }



    @GetMapping("/get-by-username/{username}")
    public ResponseEntity<?> getOneByUsername(@PathVariable String username) {
        try {
            User user = service.getOneByUsername(username);
            UserResponseDTO dto = UserMapper.toDTO(user);
            return ResponseEntity.ok(dto);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/internal/security/{username}")
    public ResponseEntity<?> getUserForSecurity(@PathVariable String username) {
        try {
            User user = service.getOneByUsername(username);
            UserSecurityResponseDTO dto = UserMapper.toSecurityDTO(user);
            return ResponseEntity.ok(dto);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/internal/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDTO dto) {
        try {
            User user = service.create(UserMapper.toEntity(dto));
            UserResponseDTO dtoNew = UserMapper.toDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(dtoNew);
        } catch (InvalidUserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserRequestDTO dto) {
        try {
            User user = service.create(UserMapper.toEntity(dto));
            UserResponseDTO dtoNew = UserMapper.toDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(dtoNew);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserRequestDTO dto) {
        try {
            User user = service.update(id, UserMapper.toEntity(dto));
            UserResponseDTO dtoNew = UserMapper.toDTO(user);
            return ResponseEntity.ok(dtoNew);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PostMapping("/add-permissions/{id}")
    public ResponseEntity<?> create(@RequestBody UserRequestDTO dto) {
        try {
            User user = service.create(UserMapper.toEntity(dto));
            UserResponseDTO dtoNew = UserMapper.toDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(dtoNew);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
