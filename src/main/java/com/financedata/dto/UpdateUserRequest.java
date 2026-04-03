package com.financedata.dto;

import com.financedata.domain.RoleName;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

	private RoleName role;

	private Boolean active;

	@Size(min = 8, max = 128)
	private String password;

	@Size(max = 255)
	private String fullName;
}
