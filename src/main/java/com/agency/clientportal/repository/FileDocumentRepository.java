package com.agency.clientportal.repository;

import com.agency.clientportal.entity.FileCategory;
import com.agency.clientportal.entity.FileDocument;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {
    List<FileDocument> findByProjectOrderByUploadedAtDesc(Project project);
    List<FileDocument> findByProjectAndCategory(Project project, FileCategory category);
    List<FileDocument> findByProjectClientOrderByUploadedAtDesc(User client);
    long countByProjectClient(User client);
}
