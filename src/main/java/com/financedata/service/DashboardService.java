package com.financedata.service;

import com.financedata.domain.FinancialRecord;
import com.financedata.domain.TransactionType;
import com.financedata.dto.DashboardSummaryResponse;
import com.financedata.dto.FinancialRecordResponse;
import com.financedata.repository.FinancialRecordRepository;
import com.financedata.repository.FinancialRecordRepository.CategoryTotalProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private static final int DEFAULT_RECENT = 10;

	private final FinancialRecordRepository financialRecordRepository;

	@Transactional(readOnly = true)
	public DashboardSummaryResponse summary(int trendMonths, int recentLimit) {
		int months = Math.max(1, Math.min(trendMonths, 120));
		int recent = Math.max(1, Math.min(recentLimit, 100));

		BigDecimal totalIncome = financialRecordRepository.sumAmountByType(TransactionType.INCOME);
		BigDecimal totalExpenses = financialRecordRepository.sumAmountByType(TransactionType.EXPENSE);
		BigDecimal net = totalIncome.subtract(totalExpenses);

		List<DashboardSummaryResponse.CategoryTotal> incomeByCategory = mapCategoryTotals(
				financialRecordRepository.sumByCategoryAndType(TransactionType.INCOME));
		List<DashboardSummaryResponse.CategoryTotal> expenseByCategory = mapCategoryTotals(
				financialRecordRepository.sumByCategoryAndType(TransactionType.EXPENSE));

		LocalDate end = LocalDate.now();
		LocalDate start = end.withDayOfMonth(1).minusMonths((long) months - 1);

		List<FinancialRecord> trendRows = financialRecordRepository.findAllForTrendBetween(start, end);
		List<DashboardSummaryResponse.MonthlyTrendPoint> trend = buildMonthlyTrend(trendRows);

		Sort sort = Sort.by(Sort.Direction.DESC, "recordDate", "createdAt");
		var recentPage = financialRecordRepository
				.findAllByDeletedFalse(PageRequest.of(0, recent, sort))
				.map(UserMapper::toRecordResponse);
		List<FinancialRecordResponse> recentActivity = recentPage.getContent();

		return DashboardSummaryResponse.builder()
				.totalIncome(totalIncome)
				.totalExpenses(totalExpenses)
				.netBalance(net)
				.incomeByCategory(incomeByCategory)
				.expensesByCategory(expenseByCategory)
				.recentActivity(recentActivity)
				.monthlyTrend(trend)
				.trendRangeStart(start)
				.trendRangeEnd(end)
				.build();
	}

	public DashboardSummaryResponse summaryDefault() {
		return summary(12, DEFAULT_RECENT);
	}

	private List<DashboardSummaryResponse.CategoryTotal> mapCategoryTotals(List<CategoryTotalProjection> rows) {
		return rows.stream()
				.map(r -> DashboardSummaryResponse.CategoryTotal.builder()
						.category(r.getCategory())
						.total(r.getTotal())
						.build())
				.toList();
	}

	private List<DashboardSummaryResponse.MonthlyTrendPoint> buildMonthlyTrend(List<FinancialRecord> rows) {
		Map<YearMonth, Aggregates> byMonth = new TreeMap<>();
		for (FinancialRecord row : rows) {
			YearMonth ym = YearMonth.from(row.getRecordDate());
			Aggregates agg = byMonth.computeIfAbsent(ym, y -> new Aggregates());
			BigDecimal amount = row.getAmount() == null ? BigDecimal.ZERO : row.getAmount();
			if (row.getType() == TransactionType.INCOME) {
				agg.income = agg.income.add(amount);
			}
			else if (row.getType() == TransactionType.EXPENSE) {
				agg.expenses = agg.expenses.add(amount);
			}
		}

		List<DashboardSummaryResponse.MonthlyTrendPoint> points = new ArrayList<>();
		for (Map.Entry<YearMonth, Aggregates> e : byMonth.entrySet()) {
			String period = String.format("%04d-%02d", e.getKey().getYear(), e.getKey().getMonthValue());
			BigDecimal income = e.getValue().income;
			BigDecimal expenses = e.getValue().expenses;
			points.add(DashboardSummaryResponse.MonthlyTrendPoint.builder()
					.period(period)
					.income(income)
					.expenses(expenses)
					.net(income.subtract(expenses))
					.build());
		}
		return points;
	}

	private static final class Aggregates {
		private BigDecimal income = BigDecimal.ZERO;
		private BigDecimal expenses = BigDecimal.ZERO;
	}
}
