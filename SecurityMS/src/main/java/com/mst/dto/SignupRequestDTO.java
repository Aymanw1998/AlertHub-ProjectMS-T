package com.mst.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {
    private String username;
    private String email;
    private String phone;
    private String password;
}
