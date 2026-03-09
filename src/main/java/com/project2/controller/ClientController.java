package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserService userService;
    private final ProjectService projectService;
    private final BidService bidService;
    private final MilestoneService milestoneService;
    private final NotificationService notificationService;

    public ClientController(UserService userService, ProjectService projectService, BidService bidService,
            MilestoneService milestoneService, NotificationService notificationService) {
        this.userService = userService;
        this.projectService = projectService;
        this.bidService = bidService;
        this.milestoneService = milestoneService;
        this.notificationService = notificationService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User client = getCurrentUser(auth);
        List<Project> projects = projectService.findByClient(client);
        model.addAttribute("user", client);
        model.addAttribute("projects", projects);
        model.addAttribute("openCount", projects.stream().filter(p -> p.getStatus() == ProjectStatus.OPEN).count());
        model.addAttribute("inProgressCount",
                projects.stream().filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS).count());
        model.addAttribute("completedCount",
                projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count());
        return "client/dashboard";
    }

    @GetMapping("/projects")
    public String myProjects(@RequestParam(required = false) String status, Authentication auth, Model model) {
        User client = getCurrentUser(auth);
        List<Project> projects = projectService.findByClient(client);

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            try {
                ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
                projects = projects.stream()
                        .filter(p -> p.getStatus() == projectStatus)
                        .toList();
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        model.addAttribute("user", client);
        model.addAttribute("projects", projects);
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        return "client/projects";
    }

    @GetMapping("/post-project")
    public String postProjectPage(Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        model.addAttribute("project", new Project());
        return "client/post-project";
    }

    @PostMapping("/post-project")
    public String postProject(@ModelAttribute Project project, Authentication auth,
            RedirectAttributes redirectAttrs) {
        User client = getCurrentUser(auth);
        project.setClient(client);
        project.setStatus(ProjectStatus.OPEN);
        Project saved = projectService.createProject(project);
        // Notify skill-matched freelancers
        notificationService.notifyMatchedFreelancers(saved);
        redirectAttrs.addFlashAttribute("success", "Project posted successfully!");
        return "redirect:/client/dashboard";
    }

    @GetMapping("/project/{id}")
    public String viewProject(@PathVariable Long id, Authentication auth, Model model) {
        Project project = projectService.findById(id).orElseThrow();
        List<Bid> bids = bidService.findByProject(project);
        model.addAttribute("user", getCurrentUser(auth));
        model.addAttribute("project", project);
        model.addAttribute("bids", bids);
        model.addAttribute("milestones", milestoneService.findByProject(project));
        return "client/project-detail";
    }

    @PostMapping("/project/{projectId}/accept-bid/{bidId}")
    public String acceptBid(@PathVariable Long projectId, @PathVariable Long bidId,
            Authentication auth, RedirectAttributes redirectAttrs) {
        Bid bid = bidService.findById(bidId).orElseThrow();
        projectService.assignFreelancer(projectId, bid.getFreelancer());
        redirectAttrs.addFlashAttribute("success", "Bid accepted! Project is now in progress.");
        return "redirect:/client/project/" + projectId;
    }

    @GetMapping("/project/{projectId}/add-milestone")
    public String addMilestonePage(@PathVariable Long projectId, Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        model.addAttribute("project", projectService.findById(projectId).orElseThrow());
        model.addAttribute("milestone", new Milestone());
        return "client/add-milestone";
    }

    @PostMapping("/project/{projectId}/add-milestone")
    public String addMilestone(@PathVariable Long projectId, @ModelAttribute Milestone milestone,
            RedirectAttributes redirectAttrs) {
        Project project = projectService.findById(projectId).orElseThrow();
        milestone.setProject(project);
        milestoneService.createMilestone(milestone);
        redirectAttrs.addFlashAttribute("success", "Milestone added!");
        return "redirect:/client/project/" + projectId;
    }

    @PostMapping("/milestone/{id}/approve")
    public String approveMilestone(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Milestone m = milestoneService.findById(id).orElseThrow();
        milestoneService.approveMilestone(id);
        redirectAttrs.addFlashAttribute("success", "Milestone approved and payment released!");
        return "redirect:/client/project/" + m.getProject().getId();
    }

    @PostMapping("/milestone/{id}/reject")
    public String rejectMilestone(@PathVariable Long id, @RequestParam String feedback,
            RedirectAttributes redirectAttrs) {
        Milestone m = milestoneService.findById(id).orElseThrow();
        milestoneService.rejectMilestone(id, feedback);
        redirectAttrs.addFlashAttribute("error", "Milestone rejected.");
        return "redirect:/client/project/" + m.getProject().getId();
    }

    @GetMapping("/wallet")
    public String wallet(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("withdrawals", userService.findWithdrawalsByUser(user));
        return "client/wallet";
    }

    @PostMapping("/wallet/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
            @RequestParam String accountName,
            @RequestParam String accountNumber,
            @RequestParam String ifscCode,
            @RequestParam String bankName,
            Authentication auth,
            RedirectAttributes redirectAttrs) {
        try {
            User user = getCurrentUser(auth);
            userService.requestWithdrawal(user, amount, accountName, accountNumber, ifscCode, bankName);
            redirectAttrs.addFlashAttribute("success", "Withdrawal request of ₹" + amount + " submitted successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/client/wallet";
    }

    @PostMapping("/wallet/add")
    public String addWallet(@RequestParam BigDecimal amount, RedirectAttributes redirectAttrs) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttrs.addFlashAttribute("error", "Please enter a valid amount.");
            return "redirect:/client/wallet";
        }
        return "redirect:/client/wallet/checkout?amount=" + amount;
    }

    @GetMapping("/wallet/checkout")
    public String checkout(@RequestParam BigDecimal amount, Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        model.addAttribute("amount", amount);
        return "client/checkout";
    }

    @PostMapping("/wallet/pay")
    public String processPayment(@RequestParam BigDecimal amount, @RequestParam String method,
            Authentication auth, RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(auth);

        // Simulating actual payment processing delay/logic
        userService.addToWallet(user, amount, "Deposit via " + method);

        redirectAttrs.addFlashAttribute("success", "✅ Payment of ₹" + amount + " successful via " + method + "!");
        return "redirect:/client/wallet";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        return "client/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, Authentication auth,
            RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(auth);
        user.setFullName(updatedUser.getFullName());
        user.setBio(updatedUser.getBio());
        user.setLocation(updatedUser.getLocation());
        user.setPhone(updatedUser.getPhone());
        userService.save(user);
        redirectAttrs.addFlashAttribute("success", "Profile updated!");
        return "redirect:/client/profile";
    }

    @PostMapping("/profile/update-photo")
    @ResponseBody
    public String updatePhoto(@RequestBody String base64Image, Authentication auth) {
        User client = getCurrentUser(auth);
        userService.updateProfilePicture(client, base64Image);
        return "Photo updated successfully";
    }
}
