package com.agency.clientportal.controller;

import com.agency.clientportal.dto.DeliverableForm;
import com.agency.clientportal.dto.DeliverableReviewForm;
import com.agency.clientportal.entity.Deliverable;
import com.agency.clientportal.entity.DeliverableStatus;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.User;
import com.agency.clientportal.service.DeliverableService;
import com.agency.clientportal.service.ProjectService;
import com.agency.clientportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/deliverables")
public class DeliverableController {

    private final DeliverableService deliverableService;
    private final ProjectService projectService;
    private final UserService userService;

    public DeliverableController(DeliverableService deliverableService,
                                 ProjectService projectService,
                                 UserService userService) {
        this.deliverableService = deliverableService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String listDeliverables(@RequestParam(value = "status", required = false) DeliverableStatus status,
                                   Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Deliverable> deliverables = deliverableService.getDeliverablesForUser(currentUser);

        if (status != null) {
            deliverables = deliverables.stream().filter(d -> d.getStatus() == status).toList();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("deliverables", deliverables);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", DeliverableStatus.values());

        return "deliverables/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newDeliverableForm(@RequestParam(value = "projectId", required = false) Long projectId,
                                     Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Project> projects = projectService.getProjectsForUser(currentUser);

        DeliverableForm form = new DeliverableForm();
        if (projectId != null) {
            form.setProjectId(projectId);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("deliverableForm", form);
        model.addAttribute("projects", projects);

        return "deliverables/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String submitDeliverable(@Valid @ModelAttribute("deliverableForm") DeliverableForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("projects", projectService.getProjectsForUser(currentUser));
            return "deliverables/form";
        }

        Deliverable created = deliverableService.submitDeliverable(form, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Deliverable '" + created.getTitle() + "' published for client review!");
        return "redirect:/projects/" + form.getProjectId() + "?tab=deliverables";
    }

    @PostMapping("/{id}/review")
    public String reviewDeliverable(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute DeliverableReviewForm reviewForm,
                                    @RequestParam(value = "redirectProjectId", required = false) Long redirectProjectId,
                                    RedirectAttributes redirectAttributes) {
        Deliverable reviewed = deliverableService.reviewDeliverable(id, reviewForm);
        String actionText = reviewed.getStatus() == DeliverableStatus.APPROVED ? "approved" : "requested changes for";
        redirectAttributes.addFlashAttribute("successMessage", "You have " + actionText + " the deliverable '" + reviewed.getTitle() + "'.");

        if (redirectProjectId != null) {
            return "redirect:/projects/" + redirectProjectId + "?tab=deliverables";
        }
        return "redirect:/deliverables";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteDeliverable(@PathVariable("id") Long id,
                                    @RequestParam(value = "redirectProjectId", required = false) Long redirectProjectId,
                                    RedirectAttributes redirectAttributes) {
        deliverableService.deleteDeliverable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Deliverable removed.");
        if (redirectProjectId != null) {
            return "redirect:/projects/" + redirectProjectId + "?tab=deliverables";
        }
        return "redirect:/deliverables";
    }
}
