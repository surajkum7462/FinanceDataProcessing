package com.financedata.repository;

import com.financedata.domain.FinancialRecord;
import com.financedata.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

	@Query("""
			select coalesce(sum(r.amount), 0) from FinancialRecord r
			where r.deleted = false and r.type = :type
			""")
	BigDecimal sumAmountByType(@Param("type") TransactionType type);

	@Query("""
			select r.category as category, coalesce(sum(r.amount), 0) as total
			from FinancialRecord r
			where r.deleted = false and r.type = :type
			group by r.category
			order by total desc
			""")
	List<CategoryTotalProjection> sumByCategoryAndType(@Param("type") TransactionType type);

	@Query("""
			select r from FinancialRecord r
			where r.deleted = false and r.recordDate between :start and :end
			""")
	List<FinancialRecord> findAllForTrendBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

	Page<FinancialRecord> findAllByDeletedFalse(Pageable pageable);

	interface CategoryTotalProjection {
		String getCategory();

		BigDecimal getTotal();
	}

}
