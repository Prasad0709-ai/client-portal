package com.agency.clientportal.repository;

import com.agency.clientportal.entity.Milestone;
import com.agency.clientportal.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProjectOrderBySortOrderAscDueDateAsc(Project project);
    long countByProjectAndCompleted(Project project, boolean completed);
}
