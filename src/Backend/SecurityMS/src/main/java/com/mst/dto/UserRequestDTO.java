package com.mst.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserRequestDTO {
    private String username;
    private String email;
    private String phone;
    private String password;
    private List<String> roles;
}
