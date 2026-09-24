package com.agency.clientportal.controller;

import com.agency.clientportal.dto.InvoiceForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.InvoiceService;
import com.agency.clientportal.service.ProjectService;
import com.agency.clientportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ProjectService projectService;
    private final UserService userService;

    public InvoiceController(InvoiceService invoiceService,
                             ProjectService projectService,
                             UserService userService) {
        this.invoiceService = invoiceService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String listInvoices(@RequestParam(value = "status", required = false) InvoiceStatus status,
                               Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Invoice> invoices = invoiceService.getInvoicesForUser(currentUser);

        if (status != null) {
            invoices = invoices.stream().filter(inv -> inv.getStatus() == status).toList();
        }

        Map<String, Object> financialSummary = invoiceService.getFinancialSummary(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("invoices", invoices);
        model.addAttribute("financialSummary", financialSummary);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", InvoiceStatus.values());

        return "invoices/list";
    }

    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Invoice invoice = invoiceService.getInvoiceById(id).orElse(null);

        if (invoice == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invoice not found.");
            return "redirect:/invoices";
        }

        // Access check: Only invoice client or admin can view
        if (!currentUser.isAdmin() && !invoice.getClient().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
            return "redirect:/invoices";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("invoice", invoice);

        return "invoices/view";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newInvoiceForm(@RequestParam(value = "projectId", required = false) Long projectId,
                                 Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Project> projects = projectService.getProjectsForUser(currentUser);
        List<User> clients = userService.findAllClients();

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber(invoiceService.generateNextInvoiceNumber());
        form.setIssuedDate(LocalDate.now());
        form.setDueDate(LocalDate.now().plusDays(15));
        if (projectId != null) {
            form.setProjectId(projectId);
            projectService.getProjectById(projectId).ifPresent(p -> form.setClientId(p.getClient().getId()));
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("invoiceForm", form);
        model.addAttribute("projects", projects);
        model.addAttribute("clients", clients);
        model.addAttribute("statuses", InvoiceStatus.values());

        return "invoices/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createInvoice(@Valid @ModelAttribute("invoiceForm") InvoiceForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("projects", projectService.getProjectsForUser(currentUser));
            model.addAttribute("clients", userService.findAllClients());
            model.addAttribute("statuses", InvoiceStatus.values());
            return "invoices/form";
        }

        Invoice created = invoiceService.createInvoice(form);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice " + created.getInvoiceNumber() + " created and issued!");
        return "redirect:/invoices/" + created.getId();
    }

    @PostMapping("/{id}/pay")
    public String processPayment(@PathVariable("id") Long id,
                                 @RequestParam(value = "paymentMethod", defaultValue = "Credit Card / Instant ACH") String paymentMethod,
                                 RedirectAttributes redirectAttributes) {
        Invoice paid = invoiceService.markAsPaid(id, paymentMethod);
        redirectAttributes.addFlashAttribute("successMessage", "Payment confirmed for invoice " + paid.getInvoiceNumber() + "! A receipt has been issued.");
        return "redirect:/invoices/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") InvoiceStatus status,
                               RedirectAttributes redirectAttributes) {
        invoiceService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice status updated to " + status.getDisplayName());
        return "redirect:/invoices/" + id;
    }
}
