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

import java.util.HashSet;
import java.util.List;
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
        assertTrue(passwordEncoder.matches("123456", saved.getPassword()));
        assertTrue(saved.getRoles().stream()
                .anyMatch(role -> "read".equals(role.getRole())));

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
    void delete_whenUserIsAdmin_throwsInvalidUserException() {
        User admin = validUser();
        admin.setId(1L);
        admin.setUsername("admin");

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(InvalidUserException.class, () -> userService.delete(1L));
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

    @Test
    void addRoles_whenRolesValid_addsRolesAndSaves() throws Exception {
        User user = validUser();
        user.setId(1L);

        Role readRole = role("read");
        Role triggerScanRole = role("triggerScan");

        user.setRoles(new HashSet<>(List.of(readRole)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findByRoleIn(List.of("triggerScan")))
                .thenReturn(List.of(triggerScanRole));
        when(userRepo.save(user)).thenReturn(user);

        User saved = userService.addRoles(1L, List.of("triggerScan"));

        assertTrue(saved.getRoles().stream()
                .anyMatch(role -> "read".equals(role.getRole())));

        assertTrue(saved.getRoles().stream()
                .anyMatch(role -> "triggerScan".equals(role.getRole())));

        verify(userRepo).save(user);
    }

    @Test
    void addRoles_whenRoleInvalid_throwsInvalidUserException() {
        User user = validUser();
        user.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findByRoleIn(List.of("notExist"))).thenReturn(List.of());

        assertThrows(
                InvalidUserException.class,
                () -> userService.addRoles(1L, List.of("notExist"))
        );
    }
    @Test
    void removeRoles_whenRoleExists_removesRoleAndSaves() throws Exception {
        User user = validUser();
        user.setId(1L);

        Role readRole = role("read");
        Role triggerScanRole = role("triggerScan");
        Role createActionRole = role("createAction");

        user.setRoles(new HashSet<>(List.of(readRole, triggerScanRole, createActionRole)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findByRoleIn(List.of("triggerScan")))
                .thenReturn(List.of(triggerScanRole));
        when(userRepo.save(user)).thenReturn(user);

        User saved = userService.removeRoles(1L, List.of("triggerScan"));

        assertTrue(saved.getRoles().stream()
                .noneMatch(role -> "triggerScan".equals(role.getRole())));

        assertTrue(saved.getRoles().stream()
                .anyMatch(role -> "read".equals(role.getRole())));

        assertTrue(saved.getRoles().stream()
                .anyMatch(role -> "createAction".equals(role.getRole())));

        verify(userRepo).save(user);
    }

    @Test
    void removeRoles_whenTryingToRemoveRead_keepsReadRole() throws Exception {
        User user = validUser();
        user.setId(1L);

        Role readRole = role("read");
        Role triggerScanRole = role("triggerScan");

        user.setRoles(new HashSet<>(List.of(readRole, triggerScanRole)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findByRoleIn(List.of("read", "triggerScan")))
                .thenReturn(List.of(readRole, triggerScanRole));
        when(userRepo.save(user)).thenReturn(user);

        User saved = userService.removeRoles(1L, List.of("read", "triggerScan"));

        assertTrue(saved.getRoles().stream()
                .anyMatch(role -> "read".equals(role.getRole())));

        assertTrue(saved.getRoles().stream()
                .noneMatch(role -> "triggerScan".equals(role.getRole())));

        verify(userRepo).save(user);
    }
}
