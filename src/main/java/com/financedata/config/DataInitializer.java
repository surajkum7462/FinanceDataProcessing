package com.financedata.config;

import com.financedata.domain.AppUser;
import com.financedata.domain.FinancialRecord;
import com.financedata.domain.RoleName;
import com.financedata.domain.TransactionType;
import com.financedata.repository.AppUserRepository;
import com.financedata.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

	private final AppUserRepository appUserRepository;
	private final FinancialRecordRepository financialRecordRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) {
		if (appUserRepository.count() > 0) {
			return;
		}

		log.info("Seeding demo users and sample financial records");

		AppUser admin = saveUser("admin@demo.local", "Admin12345!", "Admin User", RoleName.ADMIN);
		AppUser analyst = saveUser("analyst@demo.local", "Analyst12345!", "Analyst User", RoleName.ANALYST);
		saveUser("viewer@demo.local", "Viewer12345!", "Viewer User", RoleName.VIEWER);

		Instant now = Instant.now();
		createRecord(admin, new BigDecimal("5000.00"), TransactionType.INCOME, "Salary", LocalDate.now().minusMonths(1), "Monthly salary", now);
		createRecord(admin, new BigDecimal("120.50"), TransactionType.EXPENSE, "Groceries", LocalDate.now().minusWeeks(2), null, now);
		createRecord(analyst, new BigDecimal("45.00"), TransactionType.EXPENSE, "Transport", LocalDate.now().minusDays(5), "Transit pass", now);
		createRecord(admin, new BigDecimal("800.00"), TransactionType.INCOME, "Freelance", LocalDate.now().minusDays(10), "Consulting", now);
	}

	private AppUser saveUser(String email, String rawPassword, String fullName, RoleName role) {
		AppUser user = AppUser.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(rawPassword))
				.fullName(fullName)
				.role(role)
				.active(true)
				.createdAt(Instant.now())
				.build();
		return appUserRepository.save(user);
	}

	private void createRecord(
			AppUser creator,
			BigDecimal amount,
			TransactionType type,
			String category,
			LocalDate date,
			String notes,
			Instant now) {
		FinancialRecord record = FinancialRecord.builder()
				.amount(amount)
				.type(type)
				.category(category)
				.recordDate(date)
				.notes(notes)
				.createdBy(creator)
				.createdAt(now)
				.updatedAt(now)
				.deleted(false)
				.build();
		financialRecordRepository.save(record);
	}
}
