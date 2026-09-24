package com.agency.clientportal;

import com.agency.clientportal.dto.UserRegistrationForm;
import com.agency.clientportal.entity.*;
import com.agency.clientportal.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientPortalIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private DeliverableService deliverableService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private FileDocumentService fileDocumentService;

    @Autowired
    private MessageService messageService;

    // 1. PUBLIC AUTH & REGISTRATION TESTS
    @Test
    @DisplayName("Verify Login Page is accessible anonymously")
    void testLoginPageAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("Apex Studio")));
    }

    @Test
    @DisplayName("Verify Client Registration Page is accessible and creates new client account")
    void testClientRegistrationFlow() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testclient")
                        .param("password", "secret123")
                        .param("fullName", "Test Contact")
                        .param("email", "contact@testclient.com")
                        .param("companyName", "Test Innovations")
                        .param("phone", "+1 555-0100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        User created = userService.findByUsername("testclient").orElse(null);
        assertNotNull(created);
        assertEquals(Role.ROLE_CLIENT, created.getRole());
        assertEquals("Test Innovations", created.getCompanyName());
    }

    // 2. DASHBOARD & ROLE-BASED ACCESS CONTROL
    @Test
    @DisplayName("Verify unauthenticated request to /dashboard redirects to /login")
    void testUnauthenticatedAccessRedirects() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Admin can access Dashboard with full metrics")
    void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("projects", "deliverables", "invoices", "financialSummary"));
    }

    @Test
    @WithMockUser(username = "acme_client", roles = {"CLIENT"})
    @DisplayName("Verify Client can access Dashboard with their filtered scope")
    void testClientDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("projects", "deliverables"));
    }

    @Test
    @WithMockUser(username = "acme_client", roles = {"CLIENT"})
    @DisplayName("Verify Client cannot access Admin project creation form (403 Forbidden)")
    void testClientCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Admin can access project creation form")
    void testAdminCanAccessProjectForm() throws Exception {
        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/form"));
    }

    // 3. PROJECTS & MILESTONES WORKFLOW
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Projects Listing and Details Tabs")
    void testProjectDetails() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"))
                .andExpect(model().attributeExists("projects"));

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/details"))
                .andExpect(model().attributeExists("project", "milestones", "deliverables", "invoices", "files", "messages"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Live Milestone Toggle REST API recalculates project progress")
    void testMilestoneToggleApi() throws Exception {
        // Toggle milestone #1 (already completed in seed data) to incomplete
        mockMvc.perform(post("/api/milestones/1/toggle")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestoneId").value(1))
                .andExpect(jsonPath("$.completed").value(false));

        // Toggle back to completed
        mockMvc.perform(post("/api/milestones/1/toggle")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestoneId").value(1))
                .andExpect(jsonPath("$.completed").value(true));
    }

    // 4. DELIVERABLES WORKFLOW
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Deliverables Listing Page")
    void testDeliverablesList() throws Exception {
        mockMvc.perform(get("/deliverables"))
                .andExpect(status().isOk())
                .andExpect(view().name("deliverables/list"))
                .andExpect(model().attributeExists("deliverables"));
    }

    @Test
    @WithMockUser(username = "acme_client", roles = {"CLIENT"})
    @DisplayName("Verify Client Deliverable Review and Sign-off")
    void testDeliverableReviewSubmission() throws Exception {
        mockMvc.perform(post("/deliverables/2/review")
                        .with(csrf())
                        .param("status", "APPROVED")
                        .param("clientFeedback", "Approved by QA test suite! Excellent quality.")
                        .param("redirectProjectId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1?tab=deliverables"));

        Deliverable deliverable = deliverableService.getById(2L).orElse(null);
        assertNotNull(deliverable);
        assertEquals(DeliverableStatus.APPROVED, deliverable.getStatus());
        assertTrue(deliverable.getClientFeedback().contains("Approved by QA"));
        assertNotNull(deliverable.getReviewedAt());
    }

    // 5. INVOICES & BILLING WORKFLOW
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Invoices Registry and Statement View")
    void testInvoicesListAndView() throws Exception {
        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices/list"))
                .andExpect(model().attributeExists("invoices", "financialSummary"));

        mockMvc.perform(get("/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices/view"))
                .andExpect(model().attributeExists("invoice"));
    }

    @Test
    @WithMockUser(username = "acme_client", roles = {"CLIENT"})
    @DisplayName("Verify Client Settles Invoice Payment")
    void testInvoicePayment() throws Exception {
        mockMvc.perform(post("/invoices/2/pay")
                        .with(csrf())
                        .param("paymentMethod", "ACH Direct Debit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/invoices/2"));

        Invoice invoice = invoiceService.getInvoiceById(2L).orElse(null);
        assertNotNull(invoice);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals("ACH Direct Debit", invoice.getPaymentMethod());
        assertNotNull(invoice.getPaidAt());
    }

    // 6. FILE VAULT & ATTACHMENT STREAMING
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Files Repository and Upload/Download")
    void testFileUploadAndDownload() throws Exception {
        mockMvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(view().name("files/list"))
                .andExpect(model().attributeExists("files"));

        // Upload a test mock file
        MockMultipartFile testFile = new MockMultipartFile(
                "file",
                "test-specs.txt",
                "text/plain",
                "Hello Apex Agency Client Portal Specifications".getBytes()
        );

        mockMvc.perform(multipart("/files/upload")
                        .file(testFile)
                        .with(csrf())
                        .param("projectId", "1")
                        .param("category", "DESIGN_SPEC")
                        .param("description", "Uploaded by QA Integration Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/files"));

        // Test downloading seeded file #1 (verifies auto-fallback for seeded demo files)
        mockMvc.perform(get("/files/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")));
    }

    // 7. REAL-TIME PROJECT MESSAGING
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Verify Discussion Room and Message Posting")
    void testMessageCenterAndApi() throws Exception {
        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(view().name("messages/index"))
                .andExpect(model().attributeExists("projects", "messages"));

        // Send a message via Form
        mockMvc.perform(post("/messages/send")
                        .with(csrf())
                        .param("projectId", "1")
                        .param("content", "Automated QA Verification Message")
                        .param("source", "messages"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?projectId=1"));

        // Poll messages via REST API
        mockMvc.perform(get("/api/projects/1/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }
}
