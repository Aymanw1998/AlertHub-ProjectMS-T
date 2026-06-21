package com.mst.dto;

import java.util.List;

public record UserSecurityResponseDTO(
        Long id,
        String username,
        String email,
        String phone,
        String password,
        List<RoleResponseDTO> roles) {
}
