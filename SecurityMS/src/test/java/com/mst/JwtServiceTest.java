package com.mst;

import com.mst.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = JwtService.class,
        properties = {
                "jwt.secret=AlertHubSecuritySecretKey19981998",
                "jwt.expiration=3600000"
        }
)
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generateToken_containsUsernameAndRoles() {
        String token = jwtService.generateToken("ayman", 1L, List.of("read", "admin"));

        assertTrue(jwtService.isValid(token));
        assertEquals("ayman", jwtService.getUsername(token));
        assertEquals(List.of("read", "admin"), jwtService.getRoles("Bearer " + token));
    }
}
