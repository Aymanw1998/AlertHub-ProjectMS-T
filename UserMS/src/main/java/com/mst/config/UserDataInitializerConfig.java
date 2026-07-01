package com.mst.config;

import com.mst.model.Role;
import com.mst.model.User;
import com.mst.repo.RoleRepo;
import com.mst.repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
public class UserDataInitializerConfig {
    @Bean
    @Order(2)
    public CommandLineRunner initAdmin(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder) {

        return args -> {
            User admin = userRepo.findByUsername("admin").orElseGet(User::new);
            admin.setUsername("admin");
            admin.setEmail("aymanw199816@hotmail.com");
            admin.setPhone("0508241000");

            if (admin.getPassword() == null || !admin.getPassword().startsWith("$2")) {
                admin.setPassword(passwordEncoder.encode("admin"));
            }

            admin.setRoles(new HashSet<>(roleRepo.findAllByOrderById()));
            userRepo.save(admin);

            userRepo.findAll().forEach(user -> {
                if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepo.save(user);
                }
            });
        };
    }
}
