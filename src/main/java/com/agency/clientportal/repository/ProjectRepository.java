package com.agency.clientportal.repository;

import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.ProjectStatus;
import com.agency.clientportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.milestones WHERE p.client = :client ORDER BY p.createdAt DESC")
    List<Project> findByClientOrderByCreatedAtDesc(@Param("client") User client);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.milestones ORDER BY p.createdAt DESC")
    List<Project> findAllWithMilestones();

    List<Project> findByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.milestones WHERE p.id = :id")
    Project findByIdWithMilestones(@Param("id") Long id);

    long countByStatus(ProjectStatus status);
    long countByClient(User client);
    long countByClientAndStatus(User client, ProjectStatus status);
}
