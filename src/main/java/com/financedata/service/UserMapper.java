package com.financedata.service;

import com.financedata.domain.AppUser;
import com.financedata.dto.FinancialRecordResponse;
import com.financedata.dto.UserResponse;

final class UserMapper {

	private UserMapper() {
	}

	static UserResponse toResponse(AppUser user) {
		if (user == null) {
			return null;
		}
		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.fullName(user.getFullName())
				.role(user.getRole())
				.active(user.isActive())
				.createdAt(user.getCreatedAt())
				.build();
	}

	static FinancialRecordResponse.UserSummary toUserSummary(AppUser user) {
		if (user == null) {
			return null;
		}
		return FinancialRecordResponse.UserSummary.builder()
				.id(user.getId())
				.email(user.getEmail())
				.fullName(user.getFullName())
				.build();
	}

	static FinancialRecordResponse toRecordResponse(com.financedata.domain.FinancialRecord record) {
		return FinancialRecordResponse.builder()
				.id(record.getId())
				.amount(record.getAmount())
				.type(record.getType())
				.category(record.getCategory())
				.recordDate(record.getRecordDate())
				.notes(record.getNotes())
				.createdBy(toUserSummary(record.getCreatedBy()))
				.createdAt(record.getCreatedAt())
				.updatedAt(record.getUpdatedAt())
				.build();
	}
}
