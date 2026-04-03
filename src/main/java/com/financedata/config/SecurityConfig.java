package com.financedata.config;

import com.financedata.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final ObjectMapper JSON = new ObjectMapper();

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(401);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							try {
								JSON.writeValue(response.getOutputStream(), Map.of(
										"timestamp", Instant.now().toString(),
										"status", 401,
										"error", "Unauthorized",
										"message", "Authentication required"));
							}
							catch (Exception ignored) {
								response.setStatus(401);
							}
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setStatus(403);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							try {
								JSON.writeValue(response.getOutputStream(), Map.of(
										"timestamp", Instant.now().toString(),
										"status", 403,
										"error", "Forbidden",
										"message", "Insufficient permissions for this operation"));
							}
							catch (Exception ignored) {
								response.setStatus(403);
							}
						}))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/dashboard/**")
							.hasAnyRole("VIEWER", "ANALYST", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/financial-records", "/api/financial-records/**")
							.hasAnyRole("ANALYST", "ADMIN")
						.requestMatchers("/api/financial-records/**").hasRole("ADMIN")
						.requestMatchers("/api/users/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
