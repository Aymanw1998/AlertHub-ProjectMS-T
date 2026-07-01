package com.mst.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SigninRequestDTO {
    private String username;
    private String password;
}
