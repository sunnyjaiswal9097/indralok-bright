package com.indralokbright.repository;

import com.indralokbright.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    boolean existsByPoNumber(String poNumber);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(po.vendorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(po.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(po.deliverySite) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY po.createdAt DESC")
    List<PurchaseOrder> searchPurchaseOrders(@Param("keyword") String keyword);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(po.poNumber, 9) AS int)), 0) FROM PurchaseOrder po " +
           "WHERE po.poNumber LIKE CONCAT('PO-', :year, '-%')")
    Integer findMaxSequenceForYear(@Param("year") String year);

    List<PurchaseOrder> findByStatus(String status);

    long countByStatus(String status);

    List<PurchaseOrder> findByQuotationId(Long quotationId);
}
