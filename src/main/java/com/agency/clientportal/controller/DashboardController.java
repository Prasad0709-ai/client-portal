package com.agency.clientportal.controller;

import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserService userService;
    private final ProjectService projectService;
    private final DeliverableService deliverableService;
    private final InvoiceService invoiceService;
    private final FileDocumentService fileDocumentService;

    public DashboardController(UserService userService,
                               ProjectService projectService,
                               DeliverableService deliverableService,
                               InvoiceService invoiceService,
                               FileDocumentService fileDocumentService) {
        this.userService = userService;
        this.projectService = projectService;
        this.deliverableService = deliverableService;
        this.invoiceService = invoiceService;
        this.fileDocumentService = fileDocumentService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Project> projects = projectService.getProjectsForUser(currentUser);
        List<Deliverable> deliverables = deliverableService.getDeliverablesForUser(currentUser);
        List<Invoice> invoices = invoiceService.getInvoicesForUser(currentUser);
        Map<String, Object> financialSummary = invoiceService.getFinancialSummary(currentUser);

        long activeProjectsCount = projectService.countActiveProjects(currentUser);
        long pendingReviewsCount = deliverableService.countPendingReviews(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("projects", projects);
        model.addAttribute("deliverables", deliverables);
        model.addAttribute("invoices", invoices);
        model.addAttribute("financialSummary", financialSummary);
        model.addAttribute("activeProjectsCount", activeProjectsCount);
        model.addAttribute("pendingReviewsCount", pendingReviewsCount);

        return "dashboard/index";
    }
}
