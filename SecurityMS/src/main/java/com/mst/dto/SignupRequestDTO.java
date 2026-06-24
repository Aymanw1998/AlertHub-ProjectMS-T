package com.mst.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignupRequestDTO {
    private String username;
    private String email;
    private String phone;
    private String password;
}
