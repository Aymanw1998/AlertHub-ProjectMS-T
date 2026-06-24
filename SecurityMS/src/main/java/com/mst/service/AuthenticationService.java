package com.mst.service;

import com.mst.dto.RoleResponseDTO;
import com.mst.dto.SigninRequestDTO;
import com.mst.dto.SigninResponseDTO;
import com.mst.dto.SignupRequestDTO;
import com.mst.dto.SignupResponseDTO;
import com.mst.dto.UserRequestDTO;
import com.mst.dto.UserResponseDTO;
import com.mst.dto.UserSecurityResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthenticationService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private JwtService jwtService;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public SignupResponseDTO signup(SignupRequestDTO request) {
        validateSignup(request);

        UserRequestDTO userRequest = new UserRequestDTO(
                request.getUsername(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                null
        );

        try {
            UserResponseDTO user = restTemplate.postForObject(
                    userServiceUrl + "/api/user/internal/register",
                    userRequest,
                    UserResponseDTO.class
            );

            if (user == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "UserMS returned an empty response"
                );
            }

            return new SignupResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    "User registered successfully"
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    public SigninResponseDTO signin(SigninRequestDTO request) {
        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username and password are required"
            );
        }

        try {
            UserSecurityResponseDTO user = restTemplate.getForObject(
                    userServiceUrl + "/api/user/internal/security/" + request.getUsername(),
                    UserSecurityResponseDTO.class
            );

            if (user == null || !request.getPassword().equals(user.getPassword())) {
                throw invalidCredentials();
            }

            List<String> roles = user.getRoles() == null
                    ? List.of()
                    : user.getRoles().stream().map(RoleResponseDTO::getRole).toList();

            String token = jwtService.generateToken(user.getUsername(), user.getId(), roles);
            return new SigninResponseDTO(token, user.getId(), user.getUsername(), roles);
        } catch (HttpClientErrorException e) {
            throw invalidCredentials();
        }
    }

    private void validateSignup(SignupRequestDTO request) {
        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getEmail())
                || isBlank(request.getPhone())
                || isBlank(request.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username, email, phone and password are required"
            );
        }
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
