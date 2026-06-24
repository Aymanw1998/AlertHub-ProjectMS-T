package com.mst.dto;

import com.mst.model.Role;
import com.mst.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public static User toEntity(UserRequestDTO dto) {
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());

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
}
