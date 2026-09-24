package com.agency.clientportal.controller.api;

import com.agency.clientportal.dto.MessageForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RestApiController {

    private final ProjectService projectService;
    private final MilestoneService milestoneService;
    private final DeliverableService deliverableService;
    private final InvoiceService invoiceService;
    private final MessageService messageService;
    private final UserService userService;

    public RestApiController(ProjectService projectService,
                             MilestoneService milestoneService,
                             DeliverableService deliverableService,
                             InvoiceService invoiceService,
                             MessageService messageService,
                             UserService userService) {
        this.projectService = projectService;
        this.milestoneService = milestoneService;
        this.deliverableService = deliverableService;
        this.invoiceService = invoiceService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/projects/{id}/summary")
    public ResponseEntity<?> getProjectSummary(@PathVariable("id") Long id) {
        return projectService.getProjectById(id)
                .map(p -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", p.getId());
                    data.put("title", p.getTitle());
                    data.put("progress", p.getProgressPercentage());
                    data.put("status", p.getStatus().getDisplayName());
                    data.put("milestonesCount", p.getMilestones().size());
                    data.put("deliverablesCount", p.getDeliverables().size());
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/milestones/{id}/toggle")
    public ResponseEntity<?> toggleMilestone(@PathVariable("id") Long id) {
        Milestone milestone = milestoneService.toggleCompleted(id);
        Project project = milestone.getProject();

        Map<String, Object> response = new HashMap<>();
        response.put("milestoneId", milestone.getId());
        response.put("completed", milestone.isCompleted());
        response.put("projectProgress", project.getProgressPercentage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{id}/messages")
    public ResponseEntity<?> pollMessages(@PathVariable("id") Long id) {
        User user = userService.getCurrentAuthenticatedUser();
        Project project = projectService.getProjectById(id).orElse(null);

        if (project == null || user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Message> messages = messageService.getMessagesForProject(project, user);
        List<Map<String, Object>> result = messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderName", m.getSender().getFullName());
            map.put("isAdmin", m.getSender().isAdmin());
            map.put("content", m.getContent());
            map.put("sentAt", m.getSentAt().toString());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/projects/{id}/messages")
    public ResponseEntity<?> postMessage(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        User user = userService.getCurrentAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MessageForm form = new MessageForm();
        form.setProjectId(id);
        form.setContent(payload.get("content"));

        Message msg = messageService.sendMessage(form, user);
        Map<String, Object> map = new HashMap<>();
        map.put("id", msg.getId());
        map.put("senderName", msg.getSender().getFullName());
        map.put("isAdmin", msg.getSender().isAdmin());
        map.put("content", msg.getContent());
        map.put("sentAt", msg.getSentAt().toString());

        return ResponseEntity.ok(map);
    }
}
