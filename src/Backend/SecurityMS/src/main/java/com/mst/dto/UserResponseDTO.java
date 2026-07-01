package com.mst.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserResponseDTO{
    private Long id;
    private String username;
    private  String email;
    private String phone;
    private List<RoleResponseDTO> roles;
}
