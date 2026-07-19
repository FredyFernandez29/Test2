package com.fsat.fsatdesk_api.service;

import com.fsat.fsatdesk_api.model.User;
import com.fsat.fsatdesk_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreado(LocalDate.now());
        user.setPassChanged(LocalDate.now());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(String id, User updated, String newRawPassword) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setRol(updated.getRol());
        existing.setDept(updated.getDept());
        existing.setActivo(updated.isActivo());
        existing.setPerms(updated.getPerms());

        if (newRawPassword != null && !newRawPassword.isEmpty()) {
            existing.setPassword(passwordEncoder.encode(newRawPassword));
            existing.setPassChanged(LocalDate.now());
        }

        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void toggleActivo(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActivo(!user.isActivo());
        userRepository.save(user);
    }
}