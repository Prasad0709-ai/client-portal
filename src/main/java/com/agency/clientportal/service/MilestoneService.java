package com.agency.clientportal.service;

import com.agency.clientportal.dto.MilestoneForm;
import com.agency.clientportal.entity.Milestone;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.repository.MilestoneRepository;
import com.agency.clientportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }

    public List<Milestone> getMilestonesForProject(Project project) {
        return milestoneRepository.findByProjectOrderBySortOrderAscDueDateAsc(project);
    }

    public Milestone addMilestone(MilestoneForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getProjectId()));

        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(form.getTitle());
        milestone.setDescription(form.getDescription());
        milestone.setDueDate(form.getDueDate());
        milestone.setSortOrder(form.getSortOrder());
        milestone.setCompleted(form.isCompleted());

        return milestoneRepository.save(milestone);
    }

    public Milestone toggleCompleted(Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));

        milestone.setCompleted(!milestone.isCompleted());
        return milestoneRepository.save(milestone);
    }

    public void deleteMilestone(Long milestoneId) {
        milestoneRepository.deleteById(milestoneId);
    }

    public Optional<Milestone> findById(Long id) {
        return milestoneRepository.findById(id);
    }
}
