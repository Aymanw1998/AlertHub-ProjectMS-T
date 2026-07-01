package com.mst.service;

import com.mst.dto.*;
import com.mst.exceptions.InvalidUserException;
import com.mst.exceptions.UserAlreadyExistsException;
import com.mst.exceptions.UserNotFoundException;
import com.mst.model.Role;
import com.mst.model.User;
import com.mst.repo.RoleRepo;
import com.mst.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    //finalin java == const ->משתנה לא ניתן לשינוי
    private static  final String DEFAULT_ROLE = "read";

    @Autowired
    private  UserRepo userRepo;
    @Autowired
    private  RoleRepo roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<User> getAll() {
        return userRepo.findAll().stream().toList();
    }

    public User getOneById(Long id) throws UserNotFoundException {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    public User getOneByUsername(String username)
            throws UserNotFoundException, InvalidUserException {
        if (isBlank(username)) {
            throw new InvalidUserException("Username is required");
        }

        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User create(User user)
            throws InvalidUserException, UserAlreadyExistsException {

        validateRequiredFields(user, true);
        validateUnique(user.getUsername(), user.getEmail(), null);

        Set<Role> roles = user.getRoles() == null
                ? resolveRoles(null)
                : resolveRoles(user.getRoles().stream().map(r -> r.getRole()).toList());
        addReadRole(roles);
        user.setRoles(roles);
        user.setPassword(encodePasswordIfNeeded(user.getPassword()));

        return userRepo.save(user);
    }

    public User update(Long id, User info)
            throws UserNotFoundException, InvalidUserException, UserAlreadyExistsException {

        User user = getOneById(id);
        validateRequiredFields(info, false);
        validateUnique(info.getUsername(), info.getEmail(), id);

        if(!isBlank(info.getUsername())){
            user.setUsername(info.getUsername());
        }

        if(!isBlank(info.getPhone())){
            user.setPhone(info.getPhone());
        }
        if(!isBlank(info.getEmail())){
            user.setEmail(info.getEmail());
        }
        if (!isBlank(info.getPassword())) {
            user.setPassword(encodePasswordIfNeeded(info.getPassword()));
        }

        if (info.getRoles() != null) {
            Set<Role> roles = resolveRoles(info.getRoles().stream().map(r->r.getRole()).toList());
            addReadRole(roles);
            user.setRoles(roles);
        }

        return userRepo.save(user);
    }

    public void delete(Long id) throws UserNotFoundException, InvalidUserException {
        User user = getOneById(id);
        if ("admin".equals(user.getUsername())) {
            throw new InvalidUserException("Admin user cannot be deleted");
        }
        userRepo.delete(user);
    }



    private Set<Role> resolveRoles(List<String> roleNames) throws InvalidUserException {
        if (roleNames == null || roleNames.isEmpty()) {
            return new HashSet<>();
        }

        List<String> cleanNames = roleNames.stream()
                .filter(role -> role != null && !role.isBlank())
                .distinct()
                .toList();

        List<Role> roles = roleRepo.findByRoleIn(cleanNames);
        if (roles.size() != cleanNames.size()) {
            throw new InvalidUserException("One or more roles are invalid");
        }
        return new HashSet<>(roles);
    }

    private void addReadRole(Set<Role> roles) throws InvalidUserException {
        if (roles.stream().noneMatch(role -> DEFAULT_ROLE.equals(role.getRole()))) {
            roles.add(roleRepo.findByRole(DEFAULT_ROLE)
                    .orElseThrow(() -> new InvalidUserException("Default role 'read' not found")));
        }
    }

    private String encodePasswordIfNeeded(String password) {
        if (password.startsWith("$2")) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private void validateRequiredFields(User user, boolean passwordRequired)
            throws InvalidUserException {


        if (user == null
                || isBlank(user.getUsername())
                || isBlank(user.getEmail())
                || isBlank(user.getPhone())
                || (passwordRequired && isBlank(user.getPassword()))) {
            throw new InvalidUserException("Username, email, phone and password are required");
        }
    }

    private void validateUnique(String username, String email, Long currentId)
            throws UserAlreadyExistsException {
        Optional<User> sameUsername = userRepo.findByUsername(username);
        if (sameUsername.isPresent() && !sameUsername.get().getId().equals(currentId)) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        Optional<User> sameEmail = userRepo.findByEmail(email);
        if (sameEmail.isPresent() && !sameEmail.get().getId().equals(currentId)) {
            throw new UserAlreadyExistsException("Email already exists");
        }
    }

    private boolean isBlank(String filed) {
        return filed == null || filed.isBlank();
    }
}
