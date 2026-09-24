package com.agency.clientportal.controller;

import com.agency.clientportal.dto.MilestoneForm;
import com.agency.clientportal.entity.Milestone;
import com.agency.clientportal.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String addMilestone(@Valid @ModelAttribute("milestoneForm") MilestoneForm form,
                               RedirectAttributes redirectAttributes) {
        milestoneService.addMilestone(form);
        redirectAttributes.addFlashAttribute("successMessage", "Milestone added successfully!");
        return "redirect:/projects/" + form.getProjectId() + "?tab=milestones";
    }

    @PostMapping("/{id}/toggle")
    public String toggleMilestone(@PathVariable("id") Long id,
                                  @RequestParam(value = "redirectProjectId", required = false) Long redirectProjectId,
                                  RedirectAttributes redirectAttributes) {
        Milestone milestone = milestoneService.toggleCompleted(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Milestone '" + milestone.getTitle() + "' marked as " + (milestone.isCompleted() ? "Completed!" : "Pending."));

        Long projectId = redirectProjectId != null ? redirectProjectId : milestone.getProject().getId();
        return "redirect:/projects/" + projectId + "?tab=milestones";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteMilestone(@PathVariable("id") Long id,
                                  @RequestParam("projectId") Long projectId,
                                  RedirectAttributes redirectAttributes) {
        milestoneService.deleteMilestone(id);
        redirectAttributes.addFlashAttribute("successMessage", "Milestone deleted.");
        return "redirect:/projects/" + projectId + "?tab=milestones";
    }
}
