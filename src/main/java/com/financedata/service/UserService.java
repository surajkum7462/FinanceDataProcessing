package com.financedata.service;

import com.financedata.domain.AppUser;
import com.financedata.dto.CreateUserRequest;
import com.financedata.dto.UpdateUserRequest;
import com.financedata.dto.UserResponse;
import com.financedata.exception.DuplicateResourceException;
import com.financedata.exception.ResourceNotFoundException;
import com.financedata.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<UserResponse> listUsers() {
		return appUserRepository.findAll().stream()
				.map(UserMapper::toResponse)
				.sorted(this::byEmail)
				.toList();
	}

	private int byEmail(UserResponse a, UserResponse b) {
		return a.getEmail().compareToIgnoreCase(b.getEmail());
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(Long id) {
		return UserMapper.toResponse(findUser(id));
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		String email = request.getEmail().trim();
		if (appUserRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateResourceException("A user with this email already exists");
		}
		AppUser user = AppUser.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName().trim())
				.role(request.getRole())
				.active(true)
				.createdAt(Instant.now())
				.build();
		return UserMapper.toResponse(appUserRepository.save(user));
	}

	@Transactional
	public UserResponse updateUser(Long id, UpdateUserRequest request) {
		AppUser user = findUser(id);
		if (request.getFullName() != null && !request.getFullName().isBlank()) {
			user.setFullName(request.getFullName().trim());
		}
		if (request.getRole() != null) {
			user.setRole(request.getRole());
		}
		if (request.getActive() != null) {
			user.setActive(request.getActive());
		}
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		}
		return UserMapper.toResponse(user);
	}

	private AppUser findUser(Long id) {
		return appUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
