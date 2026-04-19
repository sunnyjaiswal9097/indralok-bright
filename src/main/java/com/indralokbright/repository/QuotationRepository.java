package com.indralokbright.repository;

import com.indralokbright.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    boolean existsByQuotationNumber(String quotationNumber);

    List<Quotation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM Quotation q WHERE " +
           "LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.billToCompany) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.shipToCompany) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY q.createdAt DESC")
    List<Quotation> searchQuotations(@Param("keyword") String keyword);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(q.quotationNumber, 9) AS int)), 0) FROM Quotation q " +
           "WHERE q.quotationNumber LIKE CONCAT('IB-', :year, '-%')")
    Integer findMaxSequenceForYear(@Param("year") String year);

    List<Quotation> findByStatus(String status);

    long countByStatus(String status);
}
