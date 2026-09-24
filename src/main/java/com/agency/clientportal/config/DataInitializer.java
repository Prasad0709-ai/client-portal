package com.agency.clientportal.config;

import com.agency.clientportal.entity.*;
import com.agency.clientportal.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final DeliverableRepository deliverableRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileDocumentRepository fileDocumentRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           MilestoneRepository milestoneRepository,
                           DeliverableRepository deliverableRepository,
                           InvoiceRepository invoiceRepository,
                           FileDocumentRepository fileDocumentRepository,
                           MessageRepository messageRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.deliverableRepository = deliverableRepository;
        this.invoiceRepository = invoiceRepository;
        this.fileDocumentRepository = fileDocumentRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already initialized
        }

        // 1. Create Users
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Alex Morgan");
        admin.setEmail("alex@apexagency.design");
        admin.setCompanyName("Apex Studio Agency");
        admin.setPhone("+1 (555) 019-2831");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        User acmeClient = new User();
        acmeClient.setUsername("acme_client");
        acmeClient.setPassword(passwordEncoder.encode("client123"));
        acmeClient.setFullName("Sarah Connor");
        acmeClient.setEmail("sarah@acmecorp.io");
        acmeClient.setCompanyName("Acme Global Technologies");
        acmeClient.setPhone("+1 (555) 392-8472");
        acmeClient.setRole(Role.ROLE_CLIENT);
        acmeClient.setEnabled(true);
        userRepository.save(acmeClient);

        User nexusClient = new User();
        nexusClient.setUsername("nexus_client");
        nexusClient.setPassword(passwordEncoder.encode("client123"));
        nexusClient.setFullName("David Zhang");
        nexusClient.setEmail("david@nexusai.com");
        nexusClient.setCompanyName("Nexus Intelligence Inc");
        nexusClient.setPhone("+1 (555) 839-1029");
        nexusClient.setRole(Role.ROLE_CLIENT);
        nexusClient.setEnabled(true);
        userRepository.save(nexusClient);

        // 2. Project 1: Acme Corp Web Platform
        Project acmeProject = new Project();
        acmeProject.setTitle("Acme Corp - NextGen Cloud Portal");
        acmeProject.setDescription("Complete end-to-end UX redesign, responsive dashboard architecture, and enterprise customer portal integration.");
        acmeProject.setStatus(ProjectStatus.IN_PROGRESS);
        acmeProject.setStartDate(LocalDate.now().minusDays(30));
        acmeProject.setTargetDate(LocalDate.now().plusDays(45));
        acmeProject.setBudget(BigDecimal.valueOf(38500.00));
        acmeProject.setCurrency("USD");
        acmeProject.setClient(acmeClient);
        acmeProject.setAgencyManager(admin);
        projectRepository.save(acmeProject);

        // Milestones for Project 1
        Milestone m1 = new Milestone(acmeProject, "Discovery & Architecture Blueprint", "Stakeholder interviews, technical architecture, and system requirements specification.", LocalDate.now().minusDays(20), 1);
        m1.setCompleted(true);
        milestoneRepository.save(m1);

        Milestone m2 = new Milestone(acmeProject, "Design System & Figma Tokens", "Component library, high-contrast accessible design tokens, and UI layout prototypes.", LocalDate.now().minusDays(5), 2);
        m2.setCompleted(true);
        milestoneRepository.save(m2);

        Milestone m3 = new Milestone(acmeProject, "Frontend Core Components & Auth", "Implementation of responsive navigation, authentication, and core dashboard views.", LocalDate.now().plusDays(15), 3);
        m3.setCompleted(false);
        milestoneRepository.save(m3);

        Milestone m4 = new Milestone(acmeProject, "API Integration & End-to-End Testing", "Backend service integration, automated QA validation, and load testing.", LocalDate.now().plusDays(35), 4);
        m4.setCompleted(false);
        milestoneRepository.save(m4);

        Milestone m5 = new Milestone(acmeProject, "Production Launch & Handover", "Cloud deployment, DNS cutover, client training, and documentation delivery.", LocalDate.now().plusDays(45), 5);
        m5.setCompleted(false);
        milestoneRepository.save(m5);

        // Deliverables for Project 1
        Deliverable d1 = new Deliverable(acmeProject, m2, "Interactive Figma UI Kit & Prototypes", "Complete responsive UI components and interactive click-through prototype.", "v1.2", "https://figma.com/file/demo-acme-portal", admin);
        d1.setStatus(DeliverableStatus.APPROVED);
        d1.setClientFeedback("The layout looks spectacular! Dark mode contrast is exceptionally clean. Approved!");
        d1.setReviewedAt(LocalDateTime.now().minusDays(4));
        deliverableRepository.save(d1);

        Deliverable d2 = new Deliverable(acmeProject, m3, "Client Dashboard Staging Preview", "Live staging preview build of the client dashboard featuring responsive layouts and mock data widgets.", "v0.8-beta", "https://staging.acme.apexagency.design", admin);
        d2.setStatus(DeliverableStatus.PENDING_REVIEW);
        deliverableRepository.save(d2);

        // Invoices for Project 1
        Invoice inv1 = new Invoice("INV-2026-0001", acmeProject, acmeClient, LocalDate.now().minusDays(28), LocalDate.now().minusDays(14), BigDecimal.valueOf(15000.00), BigDecimal.valueOf(10.0), "Initial 40% project kickoff retainer");
        inv1.setStatus(InvoiceStatus.PAID);
        inv1.setPaymentMethod("ACH Wire Transfer");
        inv1.setPaidAt(LocalDateTime.now().minusDays(20));
        invoiceRepository.save(inv1);

        Invoice inv2 = new Invoice("INV-2026-0002", acmeProject, acmeClient, LocalDate.now().minusDays(3), LocalDate.now().plusDays(12), BigDecimal.valueOf(12000.00), BigDecimal.valueOf(10.0), "Milestone 2 Completion: Design System & Core Dashboard Layout");
        inv2.setStatus(InvoiceStatus.SENT);
        invoiceRepository.save(inv2);

        // Files for Project 1
        FileDocument f1 = new FileDocument(acmeProject, admin, "Acme_Statement_of_Work_Executed.pdf", "seed_sow_acme.pdf", 2450000, "application/pdf", FileCategory.CONTRACT, "Signed master services agreement and milestone schedule");
        fileDocumentRepository.save(f1);

        FileDocument f2 = new FileDocument(acmeProject, admin, "Acme_Design_System_Export_v1.zip", "seed_design_system.zip", 18200000, "application/zip", FileCategory.DESIGN_SPEC, "Exported typography tokens, SVG vector icons, and color palettes");
        fileDocumentRepository.save(f2);

        // Messages for Project 1
        Message msg1 = new Message(acmeProject, admin, "Welcome to your Apex client portal, Sarah! We have officially kicked off Phase 1. You can track progress, review deliverables, and download all project files right here.");
        messageRepository.save(msg1);

        Message msg2 = new Message(acmeProject, acmeClient, "Thanks Alex! The initial Figma prototypes you submitted look incredible. Our leadership team is thrilled with the direction.");
        messageRepository.save(msg2);

        Message msg3 = new Message(acmeProject, admin, "Fantastic to hear! We just deployed the v0.8-beta staging build for your review. Take a look at the Deliverables tab whenever you get a chance.");
        messageRepository.save(msg3);


        // 3. Project 2: Nexus AI Mobile Application MVP
        Project nexusProject = new Project();
        nexusProject.setTitle("Nexus AI - iOS & Android Mobile Suite");
        nexusProject.setDescription("AI assistant mobile application with voice processing, custom LLM streaming interface, and subscription integration.");
        nexusProject.setStatus(ProjectStatus.PLANNING);
        nexusProject.setStartDate(LocalDate.now().minusDays(10));
        nexusProject.setTargetDate(LocalDate.now().plusDays(60));
        nexusProject.setBudget(BigDecimal.valueOf(26000.00));
        nexusProject.setCurrency("USD");
        nexusProject.setClient(nexusClient);
        nexusProject.setAgencyManager(admin);
        projectRepository.save(nexusProject);

        Milestone nm1 = new Milestone(nexusProject, "Brand Strategy & App Architecture", "User flow mapping, tech stack selection, and authentication flows.", LocalDate.now().minusDays(2), 1);
        nm1.setCompleted(true);
        milestoneRepository.save(nm1);

        Milestone nm2 = new Milestone(nexusProject, "High-Fidelity Wireframes & Audio UI", "Voice wave visualization screens and prompt engineering playground UI.", LocalDate.now().plusDays(14), 2);
        nm2.setCompleted(false);
        milestoneRepository.save(nm2);

        Deliverable nd1 = new Deliverable(nexusProject, nm1, "Brand Identity & Color Spectrum", "Visual design system and mobile icon mark proposals.", "v1.0", "https://nexus-brand.apexagency.design", admin);
        nd1.setStatus(DeliverableStatus.CHANGES_REQUESTED);
        nd1.setClientFeedback("The indigo shade is great, but could we try a slightly warmer gradient on the glowing orb icon?");
        nd1.setReviewedAt(LocalDateTime.now().minusDays(1));
        deliverableRepository.save(nd1);

        Invoice ninv1 = new Invoice("INV-2026-0003", nexusProject, nexusClient, LocalDate.now().minusDays(8), LocalDate.now().plusDays(7), BigDecimal.valueOf(10000.00), BigDecimal.valueOf(10.0), "Phase 1 Deposit - Brand Strategy & Wireframes");
        ninv1.setStatus(InvoiceStatus.PAID);
        ninv1.setPaymentMethod("Stripe Credit Card");
        ninv1.setPaidAt(LocalDateTime.now().minusDays(7));
        invoiceRepository.save(ninv1);

        FileDocument nf1 = new FileDocument(nexusProject, nexusClient, "Nexus_Product_Requirements_Doc.pdf", "seed_nexus_prd.pdf", 1250000, "application/pdf", FileCategory.BRIEF_REQUIREMENT, "Initial feature backlog and LLM prompt specifications");
        fileDocumentRepository.save(nf1);

        Message nmsg1 = new Message(nexusProject, nexusClient, "Hey Alex, we left notes on the icon deliverable. Really loving the dark mode styling!");
        messageRepository.save(nmsg1);
    }
}
