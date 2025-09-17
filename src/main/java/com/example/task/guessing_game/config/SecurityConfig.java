package com.example.task.guessing_game.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Permit access to these URLs without authentication
                        .requestMatchers("/", "/name-entry", "/leaderboard", "/manual-login", "/game", "/guess", "/game-over", "/css/**", "/js/**", "/h2-console/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        // Set the login page for OAuth2 (this is the default URL for Google login)
                        .loginPage("/oauth2/authorization/google")
                        // Set the URL to redirect to after successful OAuth2 login
                        .defaultSuccessUrl("/start-social", true)
                )

                // Configure logout functionality
                .logout(logout -> logout
                        // Set the URL to redirect to after logout
                        .logoutSuccessUrl("/")
                        // Allow everyone to logout
                        .permitAll()
                )

                // Disable CSRF protection for specific endpoints
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/manual-login", "/guess"))

                // Configure headers to allow H2 console to be displayed in a frame
                .headers(headers -> headers
                        // Fix for deprecated frameOptions() - use the newer approach
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        return http.build();
    }
}