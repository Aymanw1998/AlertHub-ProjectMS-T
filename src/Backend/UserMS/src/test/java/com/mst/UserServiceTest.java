package com.mst;

import com.mst.config.PasswordConfig;
import com.mst.exceptions.InvalidUserException;
import com.mst.exceptions.UserAlreadyExistsException;
import com.mst.model.Role;
import com.mst.model.User;
import com.mst.repo.RoleRepo;
import com.mst.repo.UserRepo;
import com.mst.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {UserService.class, PasswordConfig.class})
class UserServiceTest {

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private RoleRepo roleRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Test
    void create_whenUserValid_addsDefaultReadRoleAndSaves() throws Exception {
        User user = validUser();
        Role readRole = role("read");

        when(userRepo.findByUsername("ayman")).thenReturn(Optional.empty());
        when(roleRepo.findByRole("read")).thenReturn(Optional.of(readRole));
        when(userRepo.save(user)).thenReturn(user);

        User saved = userService.create(user);

        assertEquals("ayman", saved.getUsername());
        assertTrue(saved.getRoles().stream().anyMatch(role -> "read".equals(role.getRole())));
        verify(userRepo).save(user);
    }

    @Test
    void create_whenUsernameAlreadyExists_throwsUserAlreadyExistsException() {
        User user = validUser();
        User existing = validUser();
        existing.setId(1L);

        when(userRepo.findByUsername("ayman")).thenReturn(Optional.of(existing));

        assertThrows(UserAlreadyExistsException.class, () -> userService.create(user));
    }

    @Test
    void create_whenRequiredFieldMissing_throwsInvalidUserException() {
        User user = validUser();
        user.setPhone(" ");

        assertThrows(InvalidUserException.class, () -> userService.create(user));
    }

    @Test
    void getOneByUsername_whenBlank_throwsInvalidUserException() {
        assertThrows(InvalidUserException.class, () -> userService.getOneByUsername(" "));
    }

    private User validUser() {
        User user = new User();
        user.setUsername("ayman");
        user.setEmail("ayman@example.com");
        user.setPhone("+972508241000");
        user.setPassword(passwordEncoder.encode("123456"));
        return user;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setRole(name);
        return role;
    }
}
