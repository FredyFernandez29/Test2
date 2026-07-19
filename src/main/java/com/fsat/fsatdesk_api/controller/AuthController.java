package com.fsat.fsatdesk_api.controller;

import com.fsat.fsatdesk_api.dto.JwtResponse;
import com.fsat.fsatdesk_api.dto.LoginRequest;
import com.fsat.fsatdesk_api.model.User;
import com.fsat.fsatdesk_api.security.JwtTokenProvider;
import com.fsat.fsatdesk_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String jwt = tokenProvider.generateToken(authentication);

        // Verificar expiración de contraseña (90 días) - solo para no admins
        boolean passwordExpired = false;
        if (!"admin".equals(user.getRol()) && user.getPassChanged() != null) {
            long days = LocalDate.now().toEpochDay() - user.getPassChanged().toEpochDay();
            if (days > 90) passwordExpired = true;
        }

        return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getRol(), user.getPerms()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (userId == null || currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Faltan campos requeridos");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña actual incorrecta");
        }

        // Validar fortaleza (mínimo 8 caracteres, mayúscula, minúscula, número, símbolo)
        if (newPassword.length() < 8 ||
            !newPassword.matches(".*[A-Z].*") ||
            !newPassword.matches(".*[a-z].*") ||
            !newPassword.matches(".*[0-9].*") ||
            !newPassword.matches(".*[^A-Za-z0-9].*")) {
            return ResponseEntity.badRequest().body("La nueva contraseña no cumple con los requisitos de seguridad");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassChanged(LocalDate.now());
        userService.updateUser(user.getId(), user, null);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}