package com.financedata.dto;

import com.financedata.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinancialRecordRequest {

	@NotNull
	@DecimalMin(value = "0.01", inclusive = true, message = "amount must be positive")
	private BigDecimal amount;

	@NotNull
	private TransactionType type;

	@NotBlank
	@Size(max = 128)
	private String category;

	@NotNull
	private LocalDate recordDate;

	@Size(max = 2000)
	private String notes;
}
