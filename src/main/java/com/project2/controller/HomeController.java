package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    public HomeController(UserService userService, ProjectService projectService,
            NotificationService notificationService) {
        this.userService = userService;
        this.projectService = projectService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("openProjects", projectService.countOpenProjects());
        model.addAttribute("totalProjects", projectService.countTotalProjects());
        model.addAttribute("totalFreelancers", userService.countByRole(Role.FREELANCER));
        return "home";
    }

    @GetMapping("/hire/{category}")
    public String hireCategory(@PathVariable String category, Model model) {
        // Replace dashes with spaces for better matching if needed
        String displayCategory = category.replace("-", " ");
        List<User> freelancers = userService.findFreelancersByCategory(displayCategory);

        model.addAttribute("category", displayCategory);
        model.addAttribute("freelancers", freelancers);
        return "hire-category";
    }

    @GetMapping("/profile/{id}")
    public String publicProfile(@PathVariable Long id, Model model) {
        User freelancer = userService.findById(id).orElseThrow(() -> new RuntimeException("Profile not found"));
        model.addAttribute("freelancer", freelancer);
        return "public-profile";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact(org.springframework.security.core.Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            userService.findByUsername(auth.getName()).ifPresent(user -> model.addAttribute("user", user));
        }
        return "contact";
    }

    @PostMapping("/contact/send")
    public String sendContact(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String message,
            org.springframework.security.core.Authentication auth,
            RedirectAttributes redirectAttrs) {
        try {
            if (name.isBlank() || email.isBlank() || message.isBlank()) {
                redirectAttrs.addFlashAttribute("error", "All fields are required.");
                return "redirect:/contact";
            }

            // Identify user role if logged in
            String senderDetails = name.trim();
            if (auth != null && auth.isAuthenticated()) {
                User user = userService.findByUsername(auth.getName()).orElse(null);
                if (user != null) {
                    senderDetails = "[" + user.getRole() + "] " + name.trim();
                }
            }

            ContactMessage savedMsg = notificationService.saveContactMessage(name.trim(), email.trim(), message.trim());
            notificationService.notifyAdminsContactMessage(senderDetails, email.trim(), savedMsg.getId());

            redirectAttrs.addFlashAttribute("success",
                    "✅ Message sent! We'll get back to you at " + email + " soon.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error",
                    "Something went wrong. Please try again.");
        }
        return "redirect:/contact";
    }

    @GetMapping("/pricing")
    public String pricing() {
        return "pricing";
    }

    @PostMapping("/hire/freelancer/{id}")
    public String hireFreelancer(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs) {
        if (auth == null) {
            redirectAttrs.addFlashAttribute("error", "Please login to hire professionals.");
            return "redirect:/login";
        }

        User client = userService.findByUsername(auth.getName()).orElse(null);
        User freelancer = userService.findById(id).orElse(null);

        if (client == null || freelancer == null) {
            redirectAttrs.addFlashAttribute("error", "User profiles not found.");
            return "redirect:/";
        }

        if (client.getRole() != Role.CLIENT) {
            redirectAttrs.addFlashAttribute("error", "Only clients can hire professionals.");
            return "redirect:/profile/" + id;
        }

        // Notify Client
        notificationService.send(client, "✅ Request Processed",
                "Your request to hire " + freelancer.getFullName() + " has been sent successfully!",
                "HIRE_SUCCESS", "/client/dashboard");

        // Notify Freelancer
        notificationService.send(freelancer, "🎯 New Hiring Request!",
                "Client " + client.getFullName() + " wants to collaborate with you. Check your messages!",
                "NEW_HIRE", "/freelancer/dashboard");

        redirectAttrs.addFlashAttribute("success",
                "✅ Hiring request sent! " + freelancer.getFullName() + " has been notified.");

        return "redirect:/profile/" + id;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null)
            return "redirect:/login";

        model.addAttribute("user", user);
        if (user.getRole() == Role.ADMIN)
            return "redirect:/admin/dashboard";
        if (user.getRole() == Role.CLIENT)
            return "redirect:/client/dashboard";
        if (user.getRole() == Role.FREELANCER)
            return "redirect:/freelancer/dashboard";
        return "redirect:/";
    }
}
