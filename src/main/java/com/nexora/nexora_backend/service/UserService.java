package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.user.ChangePasswordRequest;
import com.nexora.nexora_backend.dto.user.CreateUserRequest;
import com.nexora.nexora_backend.dto.user.UpdateUserRoleRequest;
import com.nexora.nexora_backend.dto.user.UserResponse;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.User;
import com.nexora.nexora_backend.entity.Technician;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.UserRepository;
import com.nexora.nexora_backend.repository.TechnicianRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> PROVISIONABLE_ROLES =
            Set.of("MANAGER", "STAFF", "TECHNICIAN");

    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final OrganizationContext organizationContext;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Long organizationId = organizationContext.getOrganizationId();
        String username = request.getUsername().trim();
        String role = normalizeRole(request.getRole());
        validateProvisionableRole(role);

        Technician technician = null;
        if ("TECHNICIAN".equals(role)) {
            if (request.getTechnicianId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "technicianId is required when creating a TECHNICIAN account"
                );
            }
            technician = technicianRepository.findByIdAndOrganizationId(
                            request.getTechnicianId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician profile not found"));
            if (!"ACTIVE".equalsIgnoreCase(technician.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only active technician profiles can receive login accounts"
                );
            }
            if (technician.getUser() != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This technician profile is already linked to a user account"
                );
            }
        } else if (request.getTechnicianId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "technicianId can only be supplied for a TECHNICIAN account"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getTemporaryPassword()));
        user.setRole(role);
        user.setOrganization(entityManager.getReference(Organization.class, organizationId));
        user.setEnabled(true);
        user.setMustChangePassword(true);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            if (technician != null) {
                technician.setUser(savedUser);
                technicianRepository.save(technician);
            }
            return toResponse(savedUser);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        Long organizationId = organizationContext.getOrganizationId();
        return userRepository.findAllByOrganizationIdOrderByUsernameAsc(organizationId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findManagedUser(id));
    }

    @Transactional
    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        User user = findManagedUser(id);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Administrator roles cannot be changed through this endpoint");
        }
        String role = normalizeRole(request.getRole());
        validateProvisionableRole(role);
        if ("TECHNICIAN".equals(role) && !"TECHNICIAN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Create a technician account with technicianId so the profile is linked securely"
            );
        }
        if ("TECHNICIAN".equalsIgnoreCase(user.getRole()) && !"TECHNICIAN".equals(role)) {
            technicianRepository.findByUserUsernameAndOrganizationId(
                            user.getUsername(), organizationContext.getOrganizationId())
                    .ifPresent(technician -> {
                        technician.setUser(null);
                        technicianRepository.save(technician);
                    });
        }
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findManagedUser(id);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Administrator accounts cannot be deactivated through this endpoint");
        }
        if (isCurrentUser(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot deactivate your own account");
        }
        user.setEnabled(false);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        User user = findManagedUser(id);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Administrator accounts cannot be managed through this endpoint");
        }
        user.setEnabled(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changeOwnPassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Long organizationId = organizationContext.getOrganizationId();
        User user = userRepository.findByUsername(authentication.getName())
                .filter(candidate -> candidate.getOrganization() != null
                        && organizationId.equals(candidate.getOrganization().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "This account is deactivated");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must differ from the current password");
        }

        if (request.getNewPassword()
                .getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "New password must not exceed 72 UTF-8 bytes"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private User findManagedUser(Long id) {
        return userRepository.findByIdAndOrganizationId(
                        id, organizationContext.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isCurrentUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && user.getUsername().equals(authentication.getName());
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }

    private void validateProvisionableRole(String role) {
        if (!PROVISIONABLE_ROLES.contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be MANAGER, STAFF, or TECHNICIAN"
            );
        }
    }

    private UserResponse toResponse(User user) {
        Long organizationId = user.getOrganization() == null
                ? null : user.getOrganization().getId();
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .organizationId(organizationId)
                .enabled(user.isEnabled())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }
}
