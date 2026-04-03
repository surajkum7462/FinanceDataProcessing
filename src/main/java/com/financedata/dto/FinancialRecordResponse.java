package com.financedata.dto;

import com.financedata.domain.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Value
@Builder
public class FinancialRecordResponse {
	Long id;
	BigDecimal amount;
	TransactionType type;
	String category;
	LocalDate recordDate;
	String notes;
	UserSummary createdBy;
	Instant createdAt;
	Instant updatedAt;

	@Value
	@Builder
	public static class UserSummary {
		Long id;
		String email;
		String fullName;
	}
}
