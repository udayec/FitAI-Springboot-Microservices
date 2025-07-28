package com.fitnessai.userservice.repository;

import com.fitnessai.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);

    User findByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);

    Boolean existsByKeycloakId(String userId);

    Optional<User> findByKeycloakId(String userId);
}
