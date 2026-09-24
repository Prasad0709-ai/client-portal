package com.agency.clientportal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    private boolean readByClient = false;
    private boolean readByAgency = false;

    public Message() {
    }

    public Message(Project project, User sender, String content) {
        this.project = project;
        this.sender = sender;
        this.content = content;
        this.sentAt = LocalDateTime.now();
        if (sender.isAdmin()) {
            this.readByAgency = true;
        } else {
            this.readByClient = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isReadByClient() {
        return readByClient;
    }

    public void setReadByClient(boolean readByClient) {
        this.readByClient = readByClient;
    }

    public boolean isReadByAgency() {
        return readByAgency;
    }

    public void setReadByAgency(boolean readByAgency) {
        this.readByAgency = readByAgency;
    }
}
