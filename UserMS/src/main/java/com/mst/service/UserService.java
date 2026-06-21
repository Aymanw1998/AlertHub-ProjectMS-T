package com.mst.service;

import com.mst.dto.RoleMapper;
import com.mst.dto.RoleResponseDTO;
import com.mst.dto.UserRequestDTO;
import com.mst.dto.UserResponseDTO;
import com.mst.dto.UserSecurityResponseDTO;
import com.mst.exceptions.InvalidUserException;
import com.mst.exceptions.UserAlreadyExistsException;
import com.mst.exceptions.UserNotFoundException;
import com.mst.model.Role;
import com.mst.model.User;
import com.mst.repo.RoleRepo;
import com.mst.repo.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private static final String DEFAULT_ROLE = "read";

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepo userRepo,
            RoleRepo roleRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAll() {
        return userRepo.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponseDTO getOneById(Long id) throws UserNotFoundException {
        return toResponse(getEntity(id));
    }

    public UserResponseDTO getOneByUsername(String username)
            throws UserNotFoundException, InvalidUserException {
        return toResponse(getByUsername(username));
    }

    public UserSecurityResponseDTO getUserForSecurity(String username)
            throws UserNotFoundException, InvalidUserException {

        User user = getByUsername(username);
        return new UserSecurityResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                toRoles(user)
        );
    }

    public UserResponseDTO register(UserRequestDTO request)
            throws InvalidUserException, UserAlreadyExistsException {

        validateRequiredFields(request, true);
        validateUnique(request.getUsername(), request.getEmail(), null);

        Role readRole = roleRepo.findByRole(DEFAULT_ROLE)
                .orElseThrow(() -> new InvalidUserException("Default role 'read' not found"));

        User user = createEntity(request);
        user.setRoles(new HashSet<>(Set.of(readRole)));
        return toResponse(userRepo.save(user));
    }

    public UserResponseDTO create(UserRequestDTO request)
            throws InvalidUserException, UserAlreadyExistsException {

        validateRequiredFields(request, true);
        validateUnique(request.getUsername(), request.getEmail(), null);

        User user = createEntity(request);
        user.setRoles(resolveRoles(request.getRoles()));
        addReadRole(user.getRoles());
        return toResponse(userRepo.save(user));
    }

    public UserResponseDTO update(Long id, UserRequestDTO request)
            throws UserNotFoundException, InvalidUserException, UserAlreadyExistsException {

        User user = getEntity(id);
        validateRequiredFields(request, false);
        validateUnique(request.getUsername(), request.getEmail(), id);

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null) {
            Set<Role> roles = resolveRoles(request.getRoles());
            addReadRole(roles);
            user.setRoles(roles);
        }

        return toResponse(userRepo.save(user));
    }

    public void delete(Long id) throws UserNotFoundException, InvalidUserException {
        User user = getEntity(id);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new InvalidUserException("Admin user cannot be deleted");
        }
        userRepo.delete(user);
    }

    private User createEntity(UserRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return user;
    }

    private User getEntity(Long id) throws UserNotFoundException {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    private User getByUsername(String username)
            throws InvalidUserException, UserNotFoundException {

        if (username == null || username.isBlank()) {
            throw new InvalidUserException("Username is required");
        }

        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
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

    private void validateRequiredFields(UserRequestDTO request, boolean passwordRequired)
            throws InvalidUserException {

        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getEmail())
                || isBlank(request.getPhone())
                || (passwordRequired && isBlank(request.getPassword()))) {
            throw new InvalidUserException("Username, email, phone and password are required");
        }
    }

    private void validateUnique(String username, String email, Long currentId)
            throws UserAlreadyExistsException {

        Optional<User> sameUsername = userRepo.findByUsername(username);
        if (sameUsername.isPresent() && !sameUsername.get().getId().equals(currentId)) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepo.existsByEmail(email)) {
            boolean belongsToCurrentUser = currentId != null
                    && userRepo.findById(currentId)
                    .map(user -> email.equalsIgnoreCase(user.getEmail()))
                    .orElse(false);

            if (!belongsToCurrentUser) {
                throw new UserAlreadyExistsException("Email already exists");
            }
        }
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                toRoles(user)
        );
    }

    private List<RoleResponseDTO> toRoles(User user) {
        return user.getRoles().stream()
                .map(RoleMapper::toDTO)
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
