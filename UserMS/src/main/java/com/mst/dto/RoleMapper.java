package com.mst.dto;

import com.mst.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public static RoleResponseDTO toDTO(Role role) {
        return new RoleResponseDTO(
                role.getId(),
                role.getRole()
        );
    }

}