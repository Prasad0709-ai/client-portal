package com.agency.clientportal.controller;

import com.agency.clientportal.dto.DeliverableForm;
import com.agency.clientportal.dto.MilestoneForm;
import com.agency.clientportal.dto.ProjectForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final MilestoneService milestoneService;
    private final DeliverableService deliverableService;
    private final InvoiceService invoiceService;
    private final FileDocumentService fileDocumentService;
    private final MessageService messageService;

    public ProjectController(ProjectService projectService,
                             UserService userService,
                             MilestoneService milestoneService,
                             DeliverableService deliverableService,
                             InvoiceService invoiceService,
                             FileDocumentService fileDocumentService,
                             MessageService messageService) {
        this.projectService = projectService;
        this.userService = userService;
        this.milestoneService = milestoneService;
        this.deliverableService = deliverableService;
        this.invoiceService = invoiceService;
        this.fileDocumentService = fileDocumentService;
        this.messageService = messageService;
    }

    @GetMapping
    public String listProjects(@RequestParam(value = "status", required = false) ProjectStatus status,
                               Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Project> projects = projectService.getProjectsForUser(currentUser);

        if (status != null) {
            projects = projects.stream().filter(p -> p.getStatus() == status).toList();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("projects", projects);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ProjectStatus.values());

        return "projects/list";
    }

    @GetMapping("/{id}")
    public String projectDetails(@PathVariable("id") Long id,
                                 @RequestParam(value = "tab", defaultValue = "overview") String activeTab,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Project project = projectService.getProjectById(id).orElse(null);

        if (project == null || !projectService.canUserAccessProject(currentUser, project)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found or access denied.");
            return "redirect:/projects";
        }

        List<Milestone> milestones = milestoneService.getMilestonesForProject(project);
        List<Deliverable> deliverables = deliverableService.getDeliverablesForProject(project);
        List<Invoice> invoices = project.getInvoices();
        List<FileDocument> files = fileDocumentService.getFilesForProject(project);
        List<Message> messages = messageService.getMessagesForProject(project, currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("project", project);
        model.addAttribute("milestones", milestones);
        model.addAttribute("deliverables", deliverables);
        model.addAttribute("invoices", invoices);
        model.addAttribute("files", files);
        model.addAttribute("messages", messages);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("statuses", ProjectStatus.values());

        // For adding new sub-items inside tabs
        MilestoneForm milestoneForm = new MilestoneForm();
        milestoneForm.setProjectId(project.getId());
        model.addAttribute("milestoneForm", milestoneForm);

        DeliverableForm deliverableForm = new DeliverableForm();
        deliverableForm.setProjectId(project.getId());
        model.addAttribute("deliverableForm", deliverableForm);

        return "projects/details";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newProjectForm(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<User> clients = userService.findAllClients();

        ProjectForm form = new ProjectForm();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("projectForm", form);
        model.addAttribute("clients", clients);
        model.addAttribute("statuses", ProjectStatus.values());

        return "projects/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createProject(@Valid @ModelAttribute("projectForm") ProjectForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("clients", userService.findAllClients());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects/form";
        }

        Project created = projectService.createProject(form, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Project '" + created.getTitle() + "' successfully created!");
        return "redirect:/projects/" + created.getId();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Project project = projectService.getProjectById(id).orElse(null);
        if (project == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found.");
            return "redirect:/projects";
        }

        User currentUser = userService.getCurrentAuthenticatedUser();
        ProjectForm form = new ProjectForm();
        form.setId(project.getId());
        form.setTitle(project.getTitle());
        form.setDescription(project.getDescription());
        form.setStatus(project.getStatus());
        form.setStartDate(project.getStartDate());
        form.setTargetDate(project.getTargetDate());
        form.setBudget(project.getBudget());
        form.setCurrency(project.getCurrency());
        form.setClientId(project.getClient().getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("projectForm", form);
        model.addAttribute("clients", userService.findAllClients());
        model.addAttribute("statuses", ProjectStatus.values());

        return "projects/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateProject(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("projectForm") ProjectForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("clients", userService.findAllClients());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects/form";
        }

        projectService.updateProject(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Project updated successfully!");
        return "redirect:/projects/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") ProjectStatus status,
                               RedirectAttributes redirectAttributes) {
        projectService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Project status updated to " + status.getDisplayName());
        return "redirect:/projects/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Project removed successfully.");
        return "redirect:/projects";
    }
}
