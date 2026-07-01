package com.mst;

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

@SpringBootTest(classes = UserService.class)
class UserServiceTest {

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private RoleRepo roleRepo;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Test
    void create_whenUserValid_addsDefaultReadRoleAndSaves() throws Exception {
        User user = validUser();
        Role readRole = role("read");

        when(userRepo.findByUsername("ayman")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("ayman@example.com")).thenReturn(Optional.empty());
        when(roleRepo.findByRole("read")).thenReturn(Optional.of(readRole));
        when(passwordEncoder.encode("123456")).thenReturn("$2encoded-password");
        when(userRepo.save(user)).thenReturn(user);

        User saved = userService.create(user);

        assertEquals("ayman", saved.getUsername());
        assertEquals("$2encoded-password", saved.getPassword());
        assertTrue(saved.getRoles().stream().anyMatch(role -> "read".equals(role.getRole())));
        verify(userRepo).save(user);
    }

    @Test
    void delete_whenUserIsAdmin_throwsInvalidUserException() {
        User admin = validUser();
        admin.setId(1L);
        admin.setUsername("admin");

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(InvalidUserException.class, () -> userService.delete(1L));
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
    void create_whenEmailAlreadyExists_throwsUserAlreadyExistsException() {
        User user = validUser();
        User existing = validUser();
        existing.setId(1L);

        when(userRepo.findByUsername("ayman")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("ayman@example.com")).thenReturn(Optional.of(existing));

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
        user.setPassword("123456");
        return user;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setRole(name);
        return role;
    }
}
