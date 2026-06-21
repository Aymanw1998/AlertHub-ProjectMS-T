package com.mst.config;

import com.mst.model.Role;
import com.mst.repo.RoleRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class RoleDataInitializerConfig {
    @Bean
    @Order(1)
    public CommandLineRunner initRoles(RoleRepo roleRepo) {
        return args -> {
            createRoleIfNotExists(roleRepo, "admin");
            createRoleIfNotExists(roleRepo,"createAction");
            createRoleIfNotExists(roleRepo,"updateAction");
            createRoleIfNotExists(roleRepo,"deleteAction");
            createRoleIfNotExists(roleRepo, "createMetric");
            createRoleIfNotExists(roleRepo, "updateMetric");
            createRoleIfNotExists(roleRepo, "deleteMetric");
            createRoleIfNotExists(roleRepo, "triggerScan");
            createRoleIfNotExists(roleRepo, "triggerProcess");
            createRoleIfNotExists(roleRepo, "triggerEvaluation");
            createRoleIfNotExists(roleRepo, "read");
        };
    }

    private void createRoleIfNotExists(RoleRepo roleRepo, String roleName) {
        if (!roleRepo.existsByRole(roleName)) {
            Role role = new Role();
            role.setRole(roleName);
            roleRepo.save(role);
        }
    }
}
