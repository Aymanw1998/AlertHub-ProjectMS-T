package com.mst.dto;

import com.mst.model.Role;
import com.mst.model.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class UserMapper {

    public static User toEntity(UserRequestDTO dto) {
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        user.setRoles(toRoles(dto.getRoles()));
        return user;
    }

    public static UserResponseDTO toDTO(User user) {
        List<RoleResponseDTO> roles = user.getRoles()
                .stream()
                .map(RoleMapper::toDTO)
                .toList();

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                roles
        );
    }
    public static UserSecurityResponseDTO toSecurityDTO(User user) {
        List<RoleResponseDTO> roles = user.getRoles()
                .stream()
                .map(RoleMapper::toDTO)
                .toList();

        return new UserSecurityResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                roles
        );
    }

    private static Set<Role> toRoles(List<String> roleNames) {
        if (roleNames == null) {
            return null;
        }

        Set<Role> roles = new HashSet<>();
        roleNames.stream()
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .distinct()
                .forEach(roleName -> {
                    Role role = new Role();
                    role.setRole(roleName);
                    roles.add(role);
                });
        return roles;
    }
}
