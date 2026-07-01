package com.mst.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SigninResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private List<String> roles;
}
