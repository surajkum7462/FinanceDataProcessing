package com.financedata.controller;

import com.financedata.dto.DashboardSummaryResponse;
import com.financedata.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/summary")
	public DashboardSummaryResponse summary(
			@RequestParam(required = false, defaultValue = "12") int trendMonths,
			@RequestParam(required = false, defaultValue = "10") int recentLimit) {
		return dashboardService.summary(trendMonths, recentLimit);
	}
}
