package com.project2.controller;

import com.project2.model.Message;
import com.project2.model.User;
import com.project2.repository.MessageRepository;
import com.project2.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.project2.model.Project;
import com.project2.model.ProjectStatus;
import com.project2.repository.ProjectRepository;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ProjectRepository projectRepository;

    public ChatController(MessageRepository messageRepository, UserService userService,
            ProjectRepository projectRepository) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
    }

    private User getCurrentUser(Authentication auth) {
        if (auth == null)
            return null;
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/history/{receiverId}")
    public List<Map<String, Object>> getHistory(@PathVariable Long receiverId, Authentication auth) {
        User sender = getCurrentUser(auth);
        User receiver = userService.findById(receiverId).orElseThrow();

        List<Message> history = messageRepository.findChatHistory(sender, receiver);

        // Mark as read
        history.stream()
                .filter(m -> m.getReceiver().getId().equals(sender.getId()) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });

        return history.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSender().getId());
            map.put("content", m.getContent());
            map.put("time", m.getCreatedAt().toString());
            map.put("isMe", m.getSender().getId().equals(sender.getId()));
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestParam Long receiverId, @RequestParam String content,
            Authentication auth) {
        User sender = getCurrentUser(auth);
        User receiver = userService.findById(receiverId).orElseThrow();

        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        messageRepository.save(msg);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(Authentication auth) {
        User user = getCurrentUser(auth);
        Map<String, Object> res = new HashMap<>();
        if (user == null) {
            res.put("count", 0);
        } else {
            res.put("count", messageRepository.countUnread(user));
        }
        return res;
    }

    @GetMapping("/ai-reply")
    public Map<String, Object> getAiReply(@RequestParam String message) {
        Map<String, Object> res = new HashMap<>();
        String reply;
        message = message.toLowerCase();

        if (message.contains("hello") || message.contains("hi")) {
            reply = "Hello! I'm your Talentrock AI assistant. 🤖 I'm here to help you navigate our platform. How can I assist you today?";
        } else if (message.contains("work") || message.contains("project")) {
            List<Project> openProjects = projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.OPEN);
            if (openProjects.isEmpty()) {
                reply = "Currently, there are no open projects. 🌑 You can check periodically in the 'Browse Projects' section!";
            } else {
                StringBuilder sb = new StringBuilder(
                        "Here are some active projects you might be interested in:<br><br>");
                int count = 0;
                for (Project p : openProjects) {
                    if (count++ >= 3)
                        break;
                    sb.append("🔹 <a href='/freelancer/project/").append(p.getId())
                            .append("' style='color: #2b59ff; font-weight: 600;'>")
                            .append(p.getTitle()).append("</a><br>")
                            .append("<small style='color: #64748b;'>Budget: ₹").append(p.getBudgetMax())
                            .append("</small><br><br>");
                }
                sb.append(
                        "Visit the <a href='/freelancer/browse' style='color: var(--primary); font-weight: 600;'>Browse Projects</a> page to see more!");
                reply = sb.toString();
            }
        } else if (message.contains("hire") || message.contains("freelancer")) {
            reply = "<b>How to hire:</b><br>1. Go to your dashboard.<br>2. Click 'Post Project'.<br>3. After posting, freelancers will bid on it. You can then review their profiles and select the best fit! 🎯";
        } else if (message.contains("bid")) {
            reply = "<b>How to bid:</b><br>1. Go to 'Browse Projects'.<br>2. Click on a project that interests you.<br>3. Submit your bid amount and proposal summary. You'll get notified as soon as a client responds! 🔔";
        } else if (message.contains("payment") || message.contains("wallet") || message.contains("add money")) {
            reply = "<b>Payments & Wallet:</b><br>You can manage your funds in the 'Wallet' section. 💰 Clients can add funds via UPI or Cards to pay freelancers, while freelancers can track their earnings and request bank withdrawals.";
        } else if (message.contains("withdraw")) {
            reply = "<b>Withdrawals:</b><br>To withdraw your earnings, go to your 'Wallet' and click 'Withdraw to Bank'. 🏦 You'll need to provide your account number and IFSC code. Requests are usually processed within 24-48 hours.";
        } else if (message.contains("support") || message.contains("help") || message.contains("contact")) {
            reply = "If you need human assistance, click the 'Help & Support' link in the footer to send a message to our team, or email us at support@talentrock.com. 📧";
        } else if (message.contains("profile")) {
            reply = "You can update your name, bio, skills, and contact info in the 'My Profile' section. 👤 For freelancers, a complete profile increases your chances of being hired!";
        } else if (message.contains("safe") || message.contains("security")) {
            reply = "Talentrock uses secure payment gateways and an escrow-like milestone system to ensure both clients and freelancers are protected. 🛡️";
        } else {
            reply = "I'm not quite sure I understand that. 😅 But I can help you with: <b>Finding work, Hiring, Payments, Withdrawals,</b> or updating your <b>Profile</b>. What would you like to know more about?";
        }

        res.put("reply", reply);
        return res;
    }
}
