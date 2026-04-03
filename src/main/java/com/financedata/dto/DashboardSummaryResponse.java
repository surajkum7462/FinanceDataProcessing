package com.financedata.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class DashboardSummaryResponse {
	BigDecimal totalIncome;
	BigDecimal totalExpenses;
	BigDecimal netBalance;
	List<CategoryTotal> incomeByCategory;
	List<CategoryTotal> expensesByCategory;
	List<FinancialRecordResponse> recentActivity;
	List<MonthlyTrendPoint> monthlyTrend;
	LocalDate trendRangeStart;
	LocalDate trendRangeEnd;

	@Value
	@Builder
	public static class CategoryTotal {
		String category;
		BigDecimal total;
	}

	@Value
	@Builder
	public static class MonthlyTrendPoint {
		String period;
		BigDecimal income;
		BigDecimal expenses;
		BigDecimal net;
	}
}
