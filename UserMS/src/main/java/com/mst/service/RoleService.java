package com.mst.service;

import com.mst.dto.RoleMapper;
import com.mst.dto.RoleResponseDTO;
import com.mst.exceptions.RoleNotFoundException;
import com.mst.model.Role;
import com.mst.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepo roleRepo;

    public List<RoleResponseDTO> getAll() {
        return roleRepo.findAllByOrderByIdAsc()
                .stream()
                .map(RoleMapper::toDTO)
                .toList();
    }

    public RoleResponseDTO getOneById(Long id) throws RoleNotFoundException {
        Role role = roleRepo.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role with id " + id + " not found"));

        return RoleMapper.toDTO(role);
    }
}