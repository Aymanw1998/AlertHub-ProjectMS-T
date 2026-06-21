package com.mst.dto;

import java.util.List;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String phone,
        List<RoleResponseDTO> roles) {
}
