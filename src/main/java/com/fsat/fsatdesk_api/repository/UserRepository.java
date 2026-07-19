package com.fsat.fsatdesk_api.repository;

import com.fsat.fsatdesk_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;        
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    List<User> findByRolAndActivoTrue(String rol);   
}