package com.indralokbright.repository;

import com.indralokbright.model.MarginQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarginQuotationRepository extends JpaRepository<MarginQuotation, Long> {

    Optional<MarginQuotation> findByQuotationNumber(String quotationNumber);

    boolean existsByQuotationNumber(String quotationNumber);

    List<MarginQuotation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM MarginQuotation q WHERE " +
           "LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.billToCompany)   LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.shipToCompany)   LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "ORDER BY q.createdAt DESC")
    List<MarginQuotation> search(@Param("kw") String kw);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(q.quotationNumber,10) AS int)),0) " +
           "FROM MarginQuotation q WHERE q.quotationNumber LIKE CONCAT('IB-MQ-',:yr,'-%')")
    Integer findMaxSeq(@Param("yr") String yr);

    long countByStatus(String status);
}
