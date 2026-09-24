package com.agency.clientportal.service;

import com.agency.clientportal.dto.ProjectForm;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.ProjectStatus;
import com.agency.clientportal.entity.User;
import com.agency.clientportal.repository.ProjectRepository;
import com.agency.clientportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Project> getProjectsForUser(User user) {
        if (user.isAdmin()) {
            return projectRepository.findAllWithMilestones();
        }
        return projectRepository.findByClientOrderByCreatedAtDesc(user);
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(ProjectForm form, User manager) {
        User client = userRepository.findById(form.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + form.getClientId()));

        Project project = new Project();
        project.setTitle(form.getTitle());
        project.setDescription(form.getDescription());
        project.setStatus(form.getStatus());
        project.setStartDate(form.getStartDate());
        project.setTargetDate(form.getTargetDate());
        project.setBudget(form.getBudget());
        project.setCurrency(form.getCurrency());
        project.setClient(client);
        project.setAgencyManager(manager);

        return projectRepository.save(project);
    }

    public Project updateProject(Long id, ProjectForm form) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        User client = userRepository.findById(form.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + form.getClientId()));

        project.setTitle(form.getTitle());
        project.setDescription(form.getDescription());
        project.setStatus(form.getStatus());
        project.setStartDate(form.getStartDate());
        project.setTargetDate(form.getTargetDate());
        project.setBudget(form.getBudget());
        project.setCurrency(form.getCurrency());
        project.setClient(client);

        return projectRepository.save(project);
    }

    public void updateStatus(Long projectId, ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        project.setStatus(status);
        projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public boolean canUserAccessProject(User user, Project project) {
        if (user.isAdmin()) {
            return true;
        }
        return project.getClient() != null && project.getClient().getId().equals(user.getId());
    }

    public long countActiveProjects(User user) {
        if (user.isAdmin()) {
            return projectRepository.countByStatus(ProjectStatus.IN_PROGRESS) +
                    projectRepository.countByStatus(ProjectStatus.PLANNING) +
                    projectRepository.countByStatus(ProjectStatus.REVIEW);
        }
        return projectRepository.countByClientAndStatus(user, ProjectStatus.IN_PROGRESS) +
                projectRepository.countByClientAndStatus(user, ProjectStatus.PLANNING) +
                projectRepository.countByClientAndStatus(user, ProjectStatus.REVIEW);
    }
}
