package com.agency.clientportal.service;

import com.agency.clientportal.dto.MessageForm;
import com.agency.clientportal.entity.Message;
import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.User;
import com.agency.clientportal.repository.MessageRepository;
import com.agency.clientportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;

    public MessageService(MessageRepository messageRepository, ProjectRepository projectRepository) {
        this.messageRepository = messageRepository;
        this.projectRepository = projectRepository;
    }

    public Message sendMessage(MessageForm form, User sender) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getProjectId()));

        Message message = new Message(project, sender, form.getContent().trim());
        return messageRepository.save(message);
    }

    public List<Message> getMessagesForProject(Project project, User viewer) {
        List<Message> messages = messageRepository.findByProjectOrderBySentAtAsc(project);

        // Mark unread as read
        for (Message msg : messages) {
            if (viewer.isAdmin() && !msg.isReadByAgency()) {
                msg.setReadByAgency(true);
            } else if (!viewer.isAdmin() && !msg.isReadByClient()) {
                msg.setReadByClient(true);
            }
        }
        messageRepository.saveAll(messages);

        return messages;
    }
}
