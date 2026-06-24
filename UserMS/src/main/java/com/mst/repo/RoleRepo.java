package com.mst.repo;

import com.mst.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(String role);
    boolean existsByRole(String role);
    List<Role> findByRoleIn(List<String> roles);
    List<Role> findAllByOrderById();
}