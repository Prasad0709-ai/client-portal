package com.agency.clientportal.controller;

import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.FileDocumentService;
import com.agency.clientportal.service.ProjectService;
import com.agency.clientportal.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/files")
public class FileDocumentController {

    private final FileDocumentService fileDocumentService;
    private final ProjectService projectService;
    private final UserService userService;

    public FileDocumentController(FileDocumentService fileDocumentService,
                                  ProjectService projectService,
                                  UserService userService) {
        this.fileDocumentService = fileDocumentService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String listFiles(@RequestParam(value = "category", required = false) FileCategory category,
                            Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<FileDocument> files = fileDocumentService.getFilesForUser(currentUser);
        List<Project> projects = projectService.getProjectsForUser(currentUser);

        if (category != null) {
            files = files.stream().filter(f -> f.getCategory() == category).toList();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("files", files);
        model.addAttribute("projects", projects);
        model.addAttribute("categories", FileCategory.values());
        model.addAttribute("selectedCategory", category);

        return "files/list";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("projectId") Long projectId,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(value = "category", defaultValue = "OTHER") FileCategory category,
                             @RequestParam(value = "description", required = false) String description,
                             @RequestParam(value = "redirectProjectId", required = false) Long redirectProjectId,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Project project = projectService.getProjectById(projectId).orElse(null);

        if (project == null || !projectService.canUserAccessProject(currentUser, project)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid project selected.");
            return "redirect:/files";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a non-empty file to upload.");
            return redirectProjectId != null ? "redirect:/projects/" + redirectProjectId + "?tab=files" : "redirect:/files";
        }

        try {
            FileDocument doc = fileDocumentService.storeFile(project, currentUser, file, category, description);
            redirectAttributes.addFlashAttribute("successMessage", "File '" + doc.getFileName() + "' uploaded successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: " + ex.getMessage());
        }

        return redirectProjectId != null ? "redirect:/projects/" + redirectProjectId + "?tab=files" : "redirect:/files";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        FileDocument fileDocument = fileDocumentService.getFileById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + id));

        Resource resource = fileDocumentService.loadAsResource(fileDocument);

        String contentType = fileDocument.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDocument.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/delete")
    public String deleteFile(@PathVariable("id") Long id,
                             @RequestParam(value = "redirectProjectId", required = false) Long redirectProjectId,
                             RedirectAttributes redirectAttributes) {
        fileDocumentService.deleteFile(id);
        redirectAttributes.addFlashAttribute("successMessage", "File removed from repository.");

        if (redirectProjectId != null) {
            return "redirect:/projects/" + redirectProjectId + "?tab=files";
        }
        return "redirect:/files";
    }
}
