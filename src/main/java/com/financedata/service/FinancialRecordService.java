package com.financedata.service;

import com.financedata.domain.AppUser;
import com.financedata.domain.FinancialRecord;
import com.financedata.domain.TransactionType;
import com.financedata.dto.FinancialRecordRequest;
import com.financedata.dto.FinancialRecordResponse;
import com.financedata.exception.ResourceNotFoundException;
import com.financedata.repository.AppUserRepository;
import com.financedata.repository.FinancialRecordRepository;
import com.financedata.repository.FinancialRecordSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

	private final FinancialRecordRepository financialRecordRepository;
	private final AppUserRepository appUserRepository;

	@Transactional(readOnly = true)
	public Page<FinancialRecordResponse> list(
			LocalDate from,
			LocalDate to,
			String category,
			TransactionType type,
			Pageable pageable) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("'from' date must be on or before 'to' date");
		}
		Specification<FinancialRecord> spec = Specification.where(FinancialRecordSpecifications.notDeleted());
		if (type != null) {
			spec = spec.and(FinancialRecordSpecifications.typeEquals(type));
		}
		if (category != null && !category.isBlank()) {
			spec = spec.and(FinancialRecordSpecifications.categoryEquals(category));
		}
		spec = spec.and(FinancialRecordSpecifications.recordDateFrom(from)).and(FinancialRecordSpecifications.recordDateTo(to));

		return financialRecordRepository.findAll(spec, pageable).map(UserMapper::toRecordResponse);
	}

	@Transactional(readOnly = true)
	public FinancialRecordResponse getById(Long id) {
		return UserMapper.toRecordResponse(findActiveRecord(id));
	}

	@Transactional
	public FinancialRecordResponse create(FinancialRecordRequest request, String creatorEmail) {
		AppUser creator = appUserRepository
				.findByEmailIgnoreCase(creatorEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Instant now = Instant.now();
		FinancialRecord record = FinancialRecord.builder()
				.amount(request.getAmount())
				.type(request.getType())
				.category(request.getCategory().trim())
				.recordDate(request.getRecordDate())
				.notes(request.getNotes() == null ? null : request.getNotes().trim())
				.createdBy(creator)
				.createdAt(now)
				.updatedAt(now)
				.deleted(false)
				.build();
		return UserMapper.toRecordResponse(financialRecordRepository.save(record));
	}

	@Transactional
	public FinancialRecordResponse update(Long id, FinancialRecordRequest request) {
		FinancialRecord record = findActiveRecord(id);
		record.setAmount(request.getAmount());
		record.setType(request.getType());
		record.setCategory(request.getCategory().trim());
		record.setRecordDate(request.getRecordDate());
		record.setNotes(request.getNotes() == null ? null : request.getNotes().trim());
		record.setUpdatedAt(Instant.now());
		return UserMapper.toRecordResponse(financialRecordRepository.save(record));
	}

	@Transactional
	public void softDelete(Long id) {
		FinancialRecord record = findActiveRecord(id);
		record.setDeleted(true);
		record.setUpdatedAt(Instant.now());
		financialRecordRepository.save(record);
	}

	private FinancialRecord findActiveRecord(Long id) {
		FinancialRecord record = financialRecordRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Financial record not found"));
		if (record.isDeleted()) {
			throw new ResourceNotFoundException("Financial record not found");
		}
		return record;
	}
}
