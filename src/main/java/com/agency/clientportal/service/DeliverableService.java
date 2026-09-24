package com.agency.clientportal.service;

import com.agency.clientportal.dto.DeliverableForm;
import com.agency.clientportal.dto.DeliverableReviewForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.repository.DeliverableRepository;
import com.agency.clientportal.repository.MilestoneRepository;
import com.agency.clientportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    public DeliverableService(DeliverableRepository deliverableRepository,
                              ProjectRepository projectRepository,
                              MilestoneRepository milestoneRepository) {
        this.deliverableRepository = deliverableRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
    }

    public List<Deliverable> getDeliverablesForProject(Project project) {
        return deliverableRepository.findByProjectOrderBySubmittedAtDesc(project);
    }

    public List<Deliverable> getDeliverablesForUser(User user) {
        if (user.isAdmin()) {
            return deliverableRepository.findAll();
        }
        return deliverableRepository.findByProjectClientOrderBySubmittedAtDesc(user);
    }

    public Optional<Deliverable> getById(Long id) {
        return deliverableRepository.findById(id);
    }

    public Deliverable submitDeliverable(DeliverableForm form, User submitter) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getProjectId()));

        Milestone milestone = null;
        if (form.getMilestoneId() != null) {
            milestone = milestoneRepository.findById(form.getMilestoneId()).orElse(null);
        }

        Deliverable deliverable = new Deliverable();
        deliverable.setProject(project);
        deliverable.setMilestone(milestone);
        deliverable.setTitle(form.getTitle());
        deliverable.setDescription(form.getDescription());
        deliverable.setVersion(form.getVersion() != null ? form.getVersion() : "v1.0");
        deliverable.setAssetUrl(form.getAssetUrl());
        deliverable.setSubmittedBy(submitter);
        deliverable.setStatus(DeliverableStatus.PENDING_REVIEW);
        deliverable.setSubmittedAt(LocalDateTime.now());

        return deliverableRepository.save(deliverable);
    }

    public Deliverable reviewDeliverable(Long id, DeliverableReviewForm reviewForm) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deliverable not found: " + id));

        deliverable.setStatus(reviewForm.getStatus());
        deliverable.setClientFeedback(reviewForm.getClientFeedback());
        deliverable.setReviewedAt(LocalDateTime.now());

        return deliverableRepository.save(deliverable);
    }

    public void deleteDeliverable(Long id) {
        deliverableRepository.deleteById(id);
    }

    public long countPendingReviews(User user) {
        if (user.isAdmin()) {
            return deliverableRepository.countByStatus(DeliverableStatus.PENDING_REVIEW);
        }
        return deliverableRepository.countByProjectClientAndStatus(user, DeliverableStatus.PENDING_REVIEW);
    }
}
