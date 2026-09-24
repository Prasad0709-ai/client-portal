package com.agency.clientportal.repository;

import com.agency.clientportal.entity.Message;
import com.agency.clientportal.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByProjectOrderBySentAtAsc(Project project);
    long countByProjectAndReadByClientFalse(Project project);
    long countByProjectAndReadByAgencyFalse(Project project);
}
