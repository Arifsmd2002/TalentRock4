package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    public AdminController(UserService userService, ProjectService projectService,
            SubscriptionService subscriptionService, NotificationService notificationService) {
        this.userService = userService;
        this.projectService = projectService;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalClients", userService.countByRole(Role.CLIENT));
        model.addAttribute("totalFreelancers", userService.countByRole(Role.FREELANCER));
        model.addAttribute("totalProjects", projectService.countTotalProjects());
        model.addAttribute("openProjects", projectService.countOpenProjects());
        model.addAttribute("recentProjects", projectService.findAllProjects().stream().limit(5).toList());
        model.addAttribute("recentUsers", userService.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).limit(5).toList());
        // Subscription analytics
        model.addAttribute("activeSubscriptions", subscriptionService.countActive());
        model.addAttribute("monthlyRevenue", subscriptionService.monthlyRevenue());
        model.addAttribute("growthCount", subscriptionService.countByPlan(SubscriptionPlan.GROWTH));
        model.addAttribute("eliteCount", subscriptionService.countByPlan(SubscriptionPlan.ELITE));
        // Unread contact messages badge
        model.addAttribute("unreadMessages", notificationService.countUnreadContactMessages());
        return "admin/dashboard";
    }

    @GetMapping("/subscriptions")
    public String subscriptions(Authentication auth, Model model) {
        model.addAttribute("currentPage", "subscriptions");
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("subscriptions", subscriptionService.findAll());
        model.addAttribute("activeSubscriptions", subscriptionService.countActive());
        model.addAttribute("cancelledCount", subscriptionService.countCancelled());
        model.addAttribute("monthlyRevenue", subscriptionService.monthlyRevenue());
        model.addAttribute("growthCount", subscriptionService.countByPlan(SubscriptionPlan.GROWTH));
        model.addAttribute("proCount", subscriptionService.countByPlan(SubscriptionPlan.PRO));
        model.addAttribute("eliteCount", subscriptionService.countByPlan(SubscriptionPlan.ELITE));
        model.addAttribute("unreadMessages", notificationService.countUnreadContactMessages());
        return "admin/subscriptions";
    }

    @GetMapping("/users")
    public String users(Authentication auth, Model model) {
        model.addAttribute("currentPage", "users");
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("unreadMessages", notificationService.countUnreadContactMessages());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.toggleUserStatus(id);
        redirectAttrs.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/messages")
    public String messages(Authentication auth, Model model) {
        model.addAttribute("currentPage", "messages");
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("messages", notificationService.getAllContactMessages());
        model.addAttribute("unreadMessages", notificationService.countUnreadContactMessages());
        return "admin/messages";
    }

    @PostMapping("/messages/{id}/read")
    public String markMessageRead(@PathVariable Long id) {
        notificationService.markContactMessageRead(id);
        return "redirect:/admin/messages";
    }

    @GetMapping("/projects")
    public String projects(Authentication auth, Model model) {
        model.addAttribute("currentPage", "projects");
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("projects", projectService.findAllProjects());
        model.addAttribute("unreadMessages", notificationService.countUnreadContactMessages());
        return "admin/projects";
    }
}
