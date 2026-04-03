package com.financedata.repository;

import com.financedata.domain.FinancialRecord;
import com.financedata.domain.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class FinancialRecordSpecifications {

	private FinancialRecordSpecifications() {
	}

	public static Specification<FinancialRecord> notDeleted() {
		return (root, query, cb) -> cb.isFalse(root.get("deleted"));
	}

	public static Specification<FinancialRecord> typeEquals(TransactionType type) {
		return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
	}

	public static Specification<FinancialRecord> categoryEquals(String category) {
		return (root, query, cb) -> {
			if (category == null || category.isBlank()) {
				return cb.conjunction();
			}
			return cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
		};
	}

	public static Specification<FinancialRecord> recordDateFrom(LocalDate from) {
		return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("recordDate"), from);
	}

	public static Specification<FinancialRecord> recordDateTo(LocalDate to) {
		return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("recordDate"), to);
	}
}
