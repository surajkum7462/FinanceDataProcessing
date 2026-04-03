package com.financedata.service;

import com.financedata.domain.AppUser;
import com.financedata.dto.LoginRequest;
import com.financedata.dto.LoginResponse;
import com.financedata.dto.UserResponse;
import com.financedata.repository.AppUserRepository;
import com.financedata.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final AppUserRepository appUserRepository;

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword()));
		UserDetails principal = (UserDetails) authentication.getPrincipal();

		AppUser user = appUserRepository
				.findByEmailIgnoreCase(principal.getUsername())
				.orElseThrow();

		String token = jwtService.generateToken(principal);
		long expSeconds = jwtService.extractExpiration(token).getTime() / 1000 - Instant.now().getEpochSecond();

		UserResponse userResponse = UserMapper.toResponse(user);
		return LoginResponse.builder()
				.token(token)
				.tokenType("Bearer")
				.expiresInSeconds(Math.max(expSeconds, 0))
				.expiresAt(Instant.ofEpochMilli(jwtService.extractExpiration(token).getTime()))
				.user(userResponse)
				.build();
	}
}
