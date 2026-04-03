package com.financedata.exception;

import com.financedata.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(this::mapFieldError)
				.collect(Collectors.toList());
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.BAD_REQUEST.value())
						.error("Bad Request")
						.message("Validation failed")
						.fieldErrors(fieldErrors)
						.build());
	}

	private ApiErrorResponse.FieldError mapFieldError(FieldError fe) {
		return ApiErrorResponse.FieldError.builder()
				.field(fe.getField())
				.message(fe.getDefaultMessage())
				.build();
	}

	@ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
	public ResponseEntity<ApiErrorResponse> handleAuthFailed(AuthenticationException ex) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.UNAUTHORIZED.value())
						.error("Unauthorized")
						.message("Invalid email or password")
						.fieldErrors(List.of())
						.build());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.FORBIDDEN.value())
						.error("Forbidden")
						.message(ex.getMessage() != null ? ex.getMessage() : "Access denied")
						.fieldErrors(List.of())
						.build());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.NOT_FOUND.value())
						.error("Not Found")
						.message(ex.getMessage())
						.fieldErrors(List.of())
						.build());
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.CONFLICT.value())
						.error("Conflict")
						.message(ex.getMessage())
						.fieldErrors(List.of())
						.build());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.BAD_REQUEST.value())
						.error("Bad Request")
						.message(ex.getMessage())
						.fieldErrors(List.of())
						.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiErrorResponse.builder()
						.timestamp(Instant.now())
						.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.error("Internal Server Error")
						.message("An unexpected error occurred")
						.fieldErrors(List.of())
						.build());
	}
}
