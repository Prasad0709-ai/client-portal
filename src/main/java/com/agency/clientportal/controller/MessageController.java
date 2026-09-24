package com.agency.clientportal.controller;

import com.agency.clientportal.dto.MessageForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.MessageService;
import com.agency.clientportal.service.ProjectService;
import com.agency.clientportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final ProjectService projectService;
    private final UserService userService;

    public MessageController(MessageService messageService,
                             ProjectService projectService,
                             UserService userService) {
        this.messageService = messageService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String messageCenter(@RequestParam(value = "projectId", required = false) Long projectId,
                                Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Project> projects = projectService.getProjectsForUser(currentUser);

        Project activeProject = null;
        if (projectId != null) {
            activeProject = projects.stream().filter(p -> p.getId().equals(projectId)).findFirst().orElse(null);
        } else if (!projects.isEmpty()) {
            activeProject = projects.get(0);
        }

        List<Message> messages = activeProject != null ? messageService.getMessagesForProject(activeProject, currentUser) : List.of();

        MessageForm messageForm = new MessageForm();
        if (activeProject != null) {
            messageForm.setProjectId(activeProject.getId());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("projects", projects);
        model.addAttribute("activeProject", activeProject);
        model.addAttribute("messages", messages);
        model.addAttribute("messageForm", messageForm);

        return "messages/index";
    }

    @PostMapping("/send")
    public String sendMessage(@Valid @ModelAttribute("messageForm") MessageForm form,
                              BindingResult bindingResult,
                              @RequestParam(value = "source", defaultValue = "messages") String source,
                              RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message cannot be empty.");
            if ("projectDetails".equals(source)) {
                return "redirect:/projects/" + form.getProjectId() + "?tab=messages";
            }
            return "redirect:/messages?projectId=" + form.getProjectId();
        }

        messageService.sendMessage(form, currentUser);

        if ("projectDetails".equals(source)) {
            return "redirect:/projects/" + form.getProjectId() + "?tab=messages";
        }
        return "redirect:/messages?projectId=" + form.getProjectId();
    }
}
