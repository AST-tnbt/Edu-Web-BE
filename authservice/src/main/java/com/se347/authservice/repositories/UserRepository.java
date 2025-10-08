package com.se347.authservice.repositories;

import org.springframework.data.repository.CrudRepository;
import com.se347.authservice.entities.User;
import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
