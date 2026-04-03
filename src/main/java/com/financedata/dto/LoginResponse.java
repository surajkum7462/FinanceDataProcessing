package com.financedata.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class LoginResponse {
	String token;
	String tokenType;
	Long expiresInSeconds;
	Instant expiresAt;
	UserResponse user;
}
