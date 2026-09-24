package com.agency.clientportal.service;

import com.agency.clientportal.dto.InvoiceForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.repository.InvoiceRepository;
import com.agency.clientportal.repository.ProjectRepository;
import com.agency.clientportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Invoice> getInvoicesForUser(User user) {
        if (user.isAdmin()) {
            return invoiceRepository.findAll();
        }
        return invoiceRepository.findByClientOrderByIssuedDateDesc(user);
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Invoice createInvoice(InvoiceForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getProjectId()));

        User client = userRepository.findById(form.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + form.getClientId()));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(form.getInvoiceNumber());
        invoice.setProject(project);
        invoice.setClient(client);
        invoice.setIssuedDate(form.getIssuedDate() != null ? form.getIssuedDate() : LocalDate.now());
        invoice.setDueDate(form.getDueDate() != null ? form.getDueDate() : LocalDate.now().plusDays(15));
        invoice.setAmount(form.getAmount());
        invoice.setTaxRate(form.getTaxRate());
        invoice.setNotes(form.getNotes());
        invoice.setStatus(form.getStatus() != null ? form.getStatus() : InvoiceStatus.SENT);
        invoice.recalculateTotals();

        return invoiceRepository.save(invoice);
    }

    public Invoice markAsPaid(Long id, String paymentMethod) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(paymentMethod != null ? paymentMethod : "Credit Card / Online");
        invoice.setPaidAt(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    public void updateStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    public Map<String, Object> getFinancialSummary(User user) {
        Map<String, Object> summary = new HashMap<>();

        if (user.isAdmin()) {
            BigDecimal totalPaid = invoiceRepository.sumTotalByStatus(InvoiceStatus.PAID);
            BigDecimal totalPending = invoiceRepository.sumTotalByStatus(InvoiceStatus.SENT);
            BigDecimal totalOverdue = invoiceRepository.sumTotalByStatus(InvoiceStatus.OVERDUE);
            long countPending = invoiceRepository.countByStatus(InvoiceStatus.SENT);
            long countPaid = invoiceRepository.countByStatus(InvoiceStatus.PAID);

            summary.put("totalPaid", totalPaid != null ? totalPaid : BigDecimal.ZERO);
            summary.put("totalPending", totalPending != null ? totalPending : BigDecimal.ZERO);
            summary.put("totalOverdue", totalOverdue != null ? totalOverdue : BigDecimal.ZERO);
            summary.put("countPending", countPending);
            summary.put("countPaid", countPaid);
        } else {
            BigDecimal totalPaid = invoiceRepository.sumTotalByClientAndStatus(user, InvoiceStatus.PAID);
            BigDecimal totalPending = invoiceRepository.sumTotalByClientAndStatus(user, InvoiceStatus.SENT);
            BigDecimal totalOverdue = invoiceRepository.sumTotalByClientAndStatus(user, InvoiceStatus.OVERDUE);
            long countPending = invoiceRepository.countByClientAndStatus(user, InvoiceStatus.SENT);
            long countPaid = invoiceRepository.countByClientAndStatus(user, InvoiceStatus.PAID);

            summary.put("totalPaid", totalPaid != null ? totalPaid : BigDecimal.ZERO);
            summary.put("totalPending", totalPending != null ? totalPending : BigDecimal.ZERO);
            summary.put("totalOverdue", totalOverdue != null ? totalOverdue : BigDecimal.ZERO);
            summary.put("countPending", countPending);
            summary.put("countPaid", countPaid);
        }

        return summary;
    }

    public String generateNextInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("INV-2026-%04d", count);
    }
}
