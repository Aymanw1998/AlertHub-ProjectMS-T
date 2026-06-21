package com.mst.dto;

import java.util.List;

public record UserRequestDTO(
        String username,
        String email,
        String phone,
        String password,
        List<String> roles) {
}
