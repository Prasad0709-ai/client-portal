package com.agency.clientportal.repository;

import com.agency.clientportal.entity.Deliverable;
import com.agency.clientportal.entity.DeliverableStatus;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {
    List<Deliverable> findByProjectOrderBySubmittedAtDesc(Project project);
    List<Deliverable> findByProjectAndStatus(Project project, DeliverableStatus status);
    List<Deliverable> findByProjectClientOrderBySubmittedAtDesc(User client);
    long countByStatus(DeliverableStatus status);
    long countByProjectClientAndStatus(User client, DeliverableStatus status);
}
