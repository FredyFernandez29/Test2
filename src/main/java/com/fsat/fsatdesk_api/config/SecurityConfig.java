package com.fsat.fsatdesk_api.config;

import com.fsat.fsatdesk_api.security.CustomUserDetailsService;
import com.fsat.fsatdesk_api.security.JwtAuthenticationFilter;
import com.fsat.fsatdesk_api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // PERMITIR ACCESO A LA PÁGINA PRINCIPAL Y RECURSOS ESTÁTICOS
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/static/**").permitAll()
                // PERMITIR ENDPOINTS PÚBLICOS DE AUTENTICACIÓN
                .requestMatchers("/api/auth/login", "/api/auth/change-password").permitAll()
                // SI USAS H2 EN DESARROLLO, PERMITIR SU CONSOLA
                .requestMatchers("/h2-console/**").permitAll()
                // CUALQUIER OTRA COSA REQUIERE AUTENTICACIÓN
                .anyRequest().authenticated()
            )
            // PARA QUE LA CONSOLA H2 FUNCIONE (OPCIONAL)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}