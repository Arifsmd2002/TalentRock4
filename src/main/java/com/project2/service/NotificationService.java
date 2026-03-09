package com.project2.service;

import com.project2.model.*;
import com.project2.repository.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    public NotificationService(NotificationRepository notificationRepository,
            UserRepository userRepository,
            ContactMessageRepository contactMessageRepository,
            JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.mailSender = mailSender;
    }

    // ----------------------------------------------------------------
    // SEND A NOTIFICATION TO A SPECIFIC USER
    // ----------------------------------------------------------------
    @Transactional
    public void send(User user, String title, String message, String type, String linkUrl) {
        notificationRepository.save(new Notification(user, title, message, type, linkUrl));
    }

    // ----------------------------------------------------------------
    // NOTIFY ADMIN(S) ABOUT A CONTACT MESSAGE
    // ----------------------------------------------------------------
    @Transactional
    public void notifyAdminsContactMessage(String senderName, String senderEmail, Long msgId) {
        // Try to identify if sender is an existing user to show their role
        String senderInfo = senderName;
        com.project2.model.User sender = userRepository.findByEmail(senderEmail).orElse(null);
        if (sender != null) {
            senderInfo = "[" + sender.getRole() + "] " + senderName;
        }

        List<com.project2.model.User> admins = userRepository.findByRole(com.project2.model.Role.ADMIN);
        for (com.project2.model.User admin : admins) {
            send(admin,
                    "📬 New Support Request",
                    "New message from " + senderInfo + " (" + senderEmail + ")",
                    "CONTACT_MESSAGE",
                    "/admin/messages");
        }
    }

    // ----------------------------------------------------------------
    // NOTIFY SKILL-MATCHED FREELANCERS WHEN A PROJECT IS POSTED
    // ----------------------------------------------------------------
    @Transactional
    public void notifyMatchedFreelancers(Project project) {
        String skillsRequired = project.getSkillsRequired();
        String category = project.getCategory();

        if ((skillsRequired == null || skillsRequired.isBlank()) &&
                (category == null || category.isBlank())) {
            return; // Nothing to match on
        }

        List<User> freelancers = userRepository.findByRoleAndIsActive(Role.FREELANCER, true);

        for (User freelancer : freelancers) {
            if (isSkillMatch(freelancer, skillsRequired, category)) {
                send(freelancer,
                        "🚀 New Project Matching Your Skills!",
                        "\"" + project.getTitle() + "\" — Budget: ₹"
                                + project.getBudgetMin() + " – ₹" + project.getBudgetMax(),
                        "PROJECT_MATCH",
                        "/freelancer/browse");
            }
        }
    }

    private boolean isSkillMatch(User freelancer, String skillsRequired, String category) {
        String freelancerSkills = freelancer.getSkills() != null ? freelancer.getSkills().toLowerCase() : "";
        String freelancerCategory = freelancer.getCategory() != null ? freelancer.getCategory().toLowerCase() : "";

        // Check category match
        if (category != null && !category.isBlank()) {
            if (freelancerCategory.contains(category.toLowerCase()) ||
                    freelancerSkills.contains(category.toLowerCase())) {
                return true;
            }
        }

        // Check skill-by-skill match
        if (skillsRequired != null && !skillsRequired.isBlank()) {
            String[] requiredTokens = skillsRequired.toLowerCase().split("[,\\s]+");
            for (String token : requiredTokens) {
                if (token.length() > 2 &&
                        (freelancerSkills.contains(token) || freelancerCategory.contains(token))) {
                    return true;
                }
            }
        }
        return false;
    }

    // ----------------------------------------------------------------
    // GET NOTIFICATIONS FOR USER
    // ----------------------------------------------------------------
    public List<Notification> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Transactional
    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository
                .findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markRead(Long notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    // ----------------------------------------------------------------
    // CONTACT MESSAGES
    // ----------------------------------------------------------------
    @Transactional
    public ContactMessage saveContactMessage(String name, String email, String phone, String message, Integer rating) {
        ContactMessage cm = new ContactMessage();
        cm.setSenderName(name);
        cm.setSenderEmail(email);
        cm.setPhone(phone);
        cm.setMessage(message);
        cm.setRating(rating);
        ContactMessage saved = contactMessageRepository.save(cm);
        
        // Send email to admin
        try {
            sendEmailToAdmin(name, email, phone, message, rating);
        } catch (Exception e) {
            // Rethrow so UI can show the delivery failure
            throw new RuntimeException("Message saved but email notification failed: " + e.getMessage());
        }
        
        return saved;
    }

    private void sendEmailToAdmin(String name, String email, String phone, String message, Integer rating) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(adminEmail);
        mail.setSubject("📬 New Contact Inquiry from " + name);
        mail.setText("New Contact Form Submission:\n\n" +
                "Name: " + name + "\n" +
                "Email: " + email + "\n" +
                "Phone: " + (phone != null ? phone : "N/A") + "\n" +
                "Rating: " + (rating != null ? rating + "/5" : "N/A") + "\n\n" +
                "Message:\n" + message);
        mailSender.send(mail);
    }

    public List<ContactMessage> getAllContactMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public long countUnreadContactMessages() {
        return contactMessageRepository.countByIsRead(false);
    }

    @Transactional
    public void markContactMessageRead(Long id) {
        contactMessageRepository.findById(id).ifPresent(cm -> {
            cm.setIsRead(true);
            contactMessageRepository.save(cm);
        });
    }
}
