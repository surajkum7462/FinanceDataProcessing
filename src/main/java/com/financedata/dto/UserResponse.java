package com.financedata.dto;

import com.financedata.domain.RoleName;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserResponse {
	Long id;
	String email;
	String fullName;
	RoleName role;
	boolean active;
	Instant createdAt;
}
