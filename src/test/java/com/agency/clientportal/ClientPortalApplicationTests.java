package com.agency.clientportal;

import com.agency.clientportal.entity.Project;
import com.agency.clientportal.entity.User;
import com.agency.clientportal.service.ProjectService;
import com.agency.clientportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClientPortalApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Test
    void contextLoads() {
        assertNotNull(userService);
        assertNotNull(projectService);
    }

    @Test
    void testSeededUsersAndProjects() {
        User admin = userService.findByUsername("admin").orElse(null);
        assertNotNull(admin, "Admin user should have been initialized");
        assertTrue(admin.isAdmin(), "Admin should have ROLE_ADMIN");

        User acmeClient = userService.findByUsername("acme_client").orElse(null);
        assertNotNull(acmeClient, "Acme client user should have been initialized");
        assertFalse(acmeClient.isAdmin(), "Client should have ROLE_CLIENT");

        List<Project> clientProjects = projectService.getProjectsForUser(acmeClient);
        assertFalse(clientProjects.isEmpty(), "Acme client should have projects assigned");
        assertTrue(clientProjects.get(0).getProgressPercentage() >= 0, "Progress percentage should be non-negative");
    }
}
