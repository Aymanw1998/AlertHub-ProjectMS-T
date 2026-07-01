package com.mst;

import com.mst.exceptions.RoleNotFoundException;
import com.mst.model.Role;
import com.mst.repo.RoleRepo;
import com.mst.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RoleService.class)
class RoleServiceTest {

    @MockitoBean
    private RoleRepo roleRepo;

    @Autowired
    private RoleService roleService;

    @Test
    void getAll_returnsRolesOrderedFromRepository() {
        Role read = role("read");
        when(roleRepo.findAllByOrderById()).thenReturn(List.of(read));

        List<Role> roles = roleService.getAll();

        assertEquals(1, roles.size());
        assertEquals("read", roles.get(0).getRole());
        verify(roleRepo).findAllByOrderById();
    }

    @Test
    void getOneById_whenMissing_throwsRoleNotFoundException() {
        when(roleRepo.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> roleService.getOneById(5L));
    }

    private Role role(String name) {
        Role role = new Role();
        role.setRole(name);
        return role;
    }
}
