package com.financedata.dto;

import com.financedata.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 8, max = 128)
	private String password;

	@NotBlank
	@Size(max = 255)
	private String fullName;

	@NotNull
	private RoleName role;
}
