package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAllByOrganizationIdOrderByUsernameAsc(Long organizationId);

    Optional<User> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

}
