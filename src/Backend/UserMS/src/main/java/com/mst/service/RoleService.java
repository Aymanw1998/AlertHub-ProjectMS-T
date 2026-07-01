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

    public List<Role> getAll() {
        return roleRepo.findAllByOrderById();
    }

    public Role getOneById(Long id) throws RoleNotFoundException {
        return roleRepo.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role with id " + id + " not found"));
    }
}