package com.financedata.controller;

import com.financedata.domain.TransactionType;
import com.financedata.dto.FinancialRecordRequest;
import com.financedata.dto.FinancialRecordResponse;
import com.financedata.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financial-records")
@RequiredArgsConstructor
public class FinancialRecordController {

	private final FinancialRecordService financialRecordService;

	@GetMapping
	public Page<FinancialRecordResponse> list(
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) TransactionType type,
			@PageableDefault(size = 20, sort = "recordDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return financialRecordService.list(from, to, category, type, pageable);
	}

	@GetMapping("/{id}")
	public FinancialRecordResponse get(@PathVariable Long id) {
		return financialRecordService.getById(id);
	}

	@PostMapping
	public ResponseEntity<FinancialRecordResponse> create(
			@Valid @RequestBody FinancialRecordRequest request,
			@AuthenticationPrincipal UserDetails principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(financialRecordService.create(request, principal.getUsername()));
	}

	@PutMapping("/{id}")
	public FinancialRecordResponse update(@PathVariable Long id, @Valid @RequestBody FinancialRecordRequest request) {
		return financialRecordService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		financialRecordService.softDelete(id);
		return ResponseEntity.noContent().build();
	}
}
