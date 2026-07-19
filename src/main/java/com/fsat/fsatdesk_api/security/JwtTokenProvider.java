package com.fsat.fsatdesk_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Asegúrate de que el secreto tenga al menos 32 caracteres (ya lo tienes)
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(principal.getUsername())          // .setSubject() ahora es .subject()
                .claim("userId", principal.getId())
                .claim("rol", principal.getAuthorities().iterator().next().getAuthority())
                .issuedAt(now)                             // .setIssuedAt() -> .issuedAt()
                .expiration(expiry)                        // .setExpiration() -> .expiration()
                .signWith(key)                             // .signWith(key, algorithm) ahora solo .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)                           // Nuevo método para verificar la firma
                .build()
                .parseSignedClaims(token)                  // parseClaimsJws() ahora es parseSignedClaims()
                .getPayload();                             // .getBody() ahora es .getPayload()
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}