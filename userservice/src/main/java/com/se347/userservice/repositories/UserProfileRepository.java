package com.se347.userservice.repositories;

import com.se347.userservice.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByUserSlug(String userSlug);
    boolean existsByUserId(UUID userId);
}
