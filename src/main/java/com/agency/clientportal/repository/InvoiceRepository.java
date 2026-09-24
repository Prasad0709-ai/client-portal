package com.agency.clientportal.repository;

import com.agency.clientportal.entity.Invoice;
import com.agency.clientportal.entity.InvoiceStatus;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByClientOrderByIssuedDateDesc(User client);
    List<Invoice> findByProjectOrderByIssuedDateDesc(Project project);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.client = :client AND i.status = :status")
    BigDecimal sumTotalByClientAndStatus(@Param("client") User client, @Param("status") InvoiceStatus status);

    long countByStatus(InvoiceStatus status);
    long countByClientAndStatus(User client, InvoiceStatus status);
}
