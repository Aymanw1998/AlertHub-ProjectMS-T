package com.mst;

import com.mst.dto.RoleResponseDTO;
import com.mst.dto.SigninRequestDTO;
import com.mst.dto.SigninResponseDTO;
import com.mst.dto.SignupRequestDTO;
import com.mst.dto.SignupResponseDTO;
import com.mst.dto.UserResponseDTO;
import com.mst.dto.UserSecurityResponseDTO;
import com.mst.service.AuthenticationService;
import com.mst.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {AuthenticationService.class, JwtService.class},
        properties = {
                "user.service.url=http://localhost:1009",
                "jwt.secret=AlertHubSecuritySecretKey19981998",
                "jwt.expiration=3600000"
        }
)
class AuthenticationServiceTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void signin_whenCredentialsValid_returnsTokenAndUserData() {
        UserSecurityResponseDTO user = new UserSecurityResponseDTO(
                1L,
                "ayman",
                "ayman@example.com",
                "+972508241000",
                "$2encoded-password",
                List.of(new RoleResponseDTO(1L, "read"))
        );

        when(restTemplate.getForObject(
                "http://localhost:1009/api/user/internal/security/ayman",
                UserSecurityResponseDTO.class
        )).thenReturn(user);
        when(passwordEncoder.matches("123456", "$2encoded-password")).thenReturn(true);

        SigninResponseDTO response =
                authenticationService.signin(new SigninRequestDTO("ayman", "123456"));

        assertEquals(1L, response.getUserId());
        assertEquals("ayman", response.getUsername());
        assertEquals(List.of("read"), response.getRoles());
        assertTrue(jwtService.isValid(response.getToken()));
    }

    @Test
    void signin_whenPasswordWrong_throwsUnauthorized() {
        UserSecurityResponseDTO user = new UserSecurityResponseDTO(
                1L,
                "ayman",
                "ayman@example.com",
                "+972508241000",
                "$2encoded-password",
                List.of(new RoleResponseDTO(1L, "read"))
        );

        when(restTemplate.getForObject(
                "http://localhost:1009/api/user/internal/security/ayman",
                UserSecurityResponseDTO.class
        )).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2encoded-password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.signin(new SigninRequestDTO("ayman", "wrong"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void signup_whenRequestValid_returnsSignupResponse() {
        UserResponseDTO user = new UserResponseDTO(
                1L,
                "ayman",
                "ayman@example.com",
                "+972508241000",
                List.of(new RoleResponseDTO(1L, "read"))
        );

        when(restTemplate.postForObject(
                eq("http://localhost:1009/api/user/internal/register"),
                any(),
                eq(UserResponseDTO.class)
        )).thenReturn(user);

        SignupResponseDTO response = authenticationService.signup(
                new SignupRequestDTO("ayman", "ayman@example.com", "+972508241000", "123456")
        );

        assertEquals(1L, response.getUserId());
        assertEquals("ayman", response.getUsername());
        assertEquals("User registered successfully", response.getMessage());
    }

    @Test
    void signup_whenRequiredFieldsMissing_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.signup(new SignupRequestDTO("ayman", "", "+972508241000", "123456"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
