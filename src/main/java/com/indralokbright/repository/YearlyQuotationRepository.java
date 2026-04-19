package com.indralokbright.repository;

import com.indralokbright.model.YearlyQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YearlyQuotationRepository extends JpaRepository<YearlyQuotation, Long> {

    Optional<YearlyQuotation> findByQuotationNumber(String quotationNumber);

    boolean existsByQuotationNumber(String quotationNumber);

    List<YearlyQuotation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM YearlyQuotation q WHERE " +
           "LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.billToCompany)   LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.shipToCompany)   LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "ORDER BY q.createdAt DESC")
    List<YearlyQuotation> search(@Param("kw") String kw);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(q.quotationNumber,10) AS int)),0) " +
           "FROM YearlyQuotation q WHERE q.quotationNumber LIKE CONCAT('IB-YQ-',:yr,'-%')")
    Integer findMaxSeq(@Param("yr") String yr);

    long countByStatus(String status);
}
