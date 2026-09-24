// Apex Agency Client Portal - Interactive JavaScript

document.addEventListener('DOMContentLoaded', () => {

    // 1. Tab Switching Functionality
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const targetTab = btn.getAttribute('data-tab');

            tabBtns.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            const targetPane = document.getElementById('tab-' + targetTab);
            if (targetPane) {
                targetPane.classList.add('active');
            }

            // Update URL query parameter without reload
            const url = new URL(window.location);
            url.searchParams.set('tab', targetTab);
            window.history.replaceState({}, '', url);
        });
    });

    // 2. Chat auto-scroll
    const chatContainer = document.querySelector('.chat-messages');
    if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    // 3. Quick-fill credentials on Login page
    const demoCards = document.querySelectorAll('.demo-account-chip');
    demoCards.forEach(chip => {
        chip.addEventListener('click', () => {
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            if (usernameInput && passwordInput) {
                usernameInput.value = chip.getAttribute('data-user');
                passwordInput.value = chip.getAttribute('data-pass');
                chip.style.transform = 'scale(0.96)';
                setTimeout(() => chip.style.transform = '', 150);
            }
        });
    });

    // 4. Deliverable Review Modal Dialog
    const reviewButtons = document.querySelectorAll('.btn-review-deliverable');
    const reviewModal = document.getElementById('reviewDeliverableModal');
    const reviewForm = document.getElementById('reviewDeliverableForm');
    const reviewDeliverableTitle = document.getElementById('reviewDeliverableTitle');

    if (reviewButtons && reviewModal && reviewForm) {
        reviewButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                const title = btn.getAttribute('data-title');
                const currentFeedback = btn.getAttribute('data-feedback') || '';

                reviewForm.action = `/deliverables/${id}/review`;
                if (reviewDeliverableTitle) {
                    reviewDeliverableTitle.textContent = title;
                }
                const feedbackInput = document.getElementById('clientFeedbackInput');
                if (feedbackInput) {
                    feedbackInput.value = currentFeedback;
                }

                reviewModal.classList.add('open');
            });
        });
    }

    // 5. Payment Modal
    const payButtons = document.querySelectorAll('.btn-open-pay-modal');
    const payModal = document.getElementById('payInvoiceModal');
    if (payButtons && payModal) {
        payButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                payModal.classList.add('open');
            });
        });
    }

    // Modal Close buttons
    const modalCloseButtons = document.querySelectorAll('.modal-close, .modal-backdrop');
    modalCloseButtons.forEach(element => {
        element.addEventListener('click', (e) => {
            if (e.target === element || e.target.classList.contains('modal-close')) {
                document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.remove('open'));
            }
        });
    });

    // 6. Live Milestone AJAX toggle
    const milestoneCheckboxes = document.querySelectorAll('.milestone-ajax-toggle');
    milestoneCheckboxes.forEach(box => {
        box.addEventListener('change', async (e) => {
            const milestoneId = box.getAttribute('data-id');
            const timelineItem = box.closest('.timeline-item');

            try {
                const response = await fetch(`/api/milestones/${milestoneId}/toggle`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });

                if (response.ok) {
                    const data = await response.json();
                    if (timelineItem) {
                        if (data.completed) {
                            timelineItem.classList.add('completed');
                        } else {
                            timelineItem.classList.remove('completed');
                        }
                    }

                    // Update project progress bar if present on page
                    const progressBars = document.querySelectorAll('.project-progress-fill');
                    const progressTexts = document.querySelectorAll('.project-progress-text');
                    progressBars.forEach(bar => {
                        bar.style.width = data.projectProgress + '%';
                    });
                    progressTexts.forEach(txt => {
                        txt.textContent = data.projectProgress + '%';
                    });
                }
            } catch (err) {
                console.error("Failed to toggle milestone:", err);
            }
        });
    });
});
