package com.mst.controller;

import com.mst.dto.SigninRequestDTO;
import com.mst.dto.SigninResponseDTO;
import com.mst.dto.SignupRequestDTO;
import com.mst.dto.SignupResponseDTO;
import com.mst.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;
    

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDTO> signin(@RequestBody SigninRequestDTO request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }
}
