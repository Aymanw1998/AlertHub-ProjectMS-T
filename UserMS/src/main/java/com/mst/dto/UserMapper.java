package com.mst.dto;

import com.mst.model.Role;
import com.mst.model.User;
import org.springframework.stereotype.Component;

import java.util.List;


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

    public static void updateEntity(User user, UserRequestDTO dto) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }
    }

}
