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
@RequestMapping("/freelancer")
public class FreelancerController {

    private final UserService userService;
    private final ProjectService projectService;
    private final BidService bidService;
    private final MilestoneService milestoneService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    public FreelancerController(UserService userService, ProjectService projectService, BidService bidService,
            MilestoneService milestoneService, NotificationService notificationService,
            SubscriptionService subscriptionService) {
        this.userService = userService;
        this.projectService = projectService;
        this.bidService = bidService;
        this.milestoneService = milestoneService;
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        List<Bid> myBids = bidService.findByFreelancer(freelancer);
        List<Project> activeProjects = projectService.findByFreelancer(freelancer);

        model.addAttribute("user", freelancer);
        model.addAttribute("myBids", myBids);
        model.addAttribute("activeProjects", activeProjects);
        model.addAttribute("pendingBids", myBids.stream().filter(b -> b.getStatus() == BidStatus.PENDING).count());
        model.addAttribute("acceptedBids", myBids.stream().filter(b -> b.getStatus() == BidStatus.ACCEPTED).count());

        // Add subscription info
        Subscription activeSub = subscriptionService.findActive(freelancer).orElse(null);
        model.addAttribute("activeSubscription", activeSub);
        model.addAttribute("remainingBids", subscriptionService.getRemainingBids(freelancer));

        return "freelancer/dashboard";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String keyword, Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        model.addAttribute("projects", projectService.searchProjects(keyword));
        model.addAttribute("keyword", keyword);
        return "freelancer/browse";
    }

    @GetMapping("/project/{id}")
    public String viewProject(@PathVariable Long id, Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        Project project = projectService.findById(id).orElseThrow();
        boolean alreadyBid = bidService.findByProject(project)
                .stream().anyMatch(b -> b.getFreelancer().getId().equals(freelancer.getId()));

        model.addAttribute("user", freelancer);
        model.addAttribute("project", project);
        model.addAttribute("alreadyBid", alreadyBid);
        model.addAttribute("bid", new Bid());
        model.addAttribute("remainingBids", subscriptionService.getRemainingBids(freelancer));
        return "freelancer/project-detail";
    }

    @PostMapping("/project/{id}/bid")
    public String submitBid(@PathVariable Long id, @ModelAttribute Bid bid,
            Authentication auth, RedirectAttributes redirectAttrs) {
        User freelancer = getCurrentUser(auth);
        Project project = projectService.findById(id).orElseThrow();

        int remaining = subscriptionService.getRemainingBids(freelancer);
        if (remaining <= 0) {
            redirectAttrs.addFlashAttribute("error",
                    "You have exhausted your monthly bid limit. Upgrade your plan to bid more!");
            return "redirect:/freelancer/project/" + id;
        }

        bid.setFreelancer(freelancer);
        bid.setProject(project);
        try {
            bidService.submitBid(bid);
            subscriptionService.useBid(freelancer);

            // Notify the client instantly
            notificationService.send(
                    project.getClient(),
                    "🎯 New Bid Received!",
                    freelancer.getFullName() + " has bid ₹" + bid.getBidAmount() + " on your project \""
                            + project.getTitle() + "\"",
                    "NEW_BID",
                    "/client/project/" + project.getId());

            int updatedRemaining = remaining - 1;
            String remainingMsg = (updatedRemaining == Integer.MAX_VALUE - 1) ? "unlimited"
                    : String.valueOf(updatedRemaining);
            redirectAttrs.addFlashAttribute("success",
                    "Bid submitted successfully! Remaining bids this month: " + remainingMsg);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/freelancer/browse";
    }

    @GetMapping("/active-project/{id}")
    public String activeProject(@PathVariable Long id, Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        Project project = projectService.findById(id).orElseThrow();
        model.addAttribute("user", freelancer);
        model.addAttribute("project", project);
        model.addAttribute("milestones", milestoneService.findByProject(project));
        return "freelancer/active-project";
    }

    @PostMapping("/milestone/{id}/submit")
    public String submitMilestone(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Milestone m = milestoneService.findById(id).orElseThrow();
        milestoneService.submitMilestone(id);
        redirectAttrs.addFlashAttribute("success", "Milestone submitted for review!");
        return "redirect:/freelancer/active-project/" + m.getProject().getId();
    }

    @GetMapping("/bids")
    public String bids(@RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        BidStatus bidStatus = null;
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            bidStatus = BidStatus.valueOf(status);
        }

        List<Bid> bids = bidService.searchBids(freelancer, bidStatus, search);

        model.addAttribute("user", freelancer);
        model.addAttribute("bids", bids);
        model.addAttribute("status", status != null ? status : "ALL");
        model.addAttribute("search", search);
        return "freelancer/bids";
    }

    @GetMapping("/wallet")
    public String wallet(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("withdrawals", userService.findWithdrawalsByUser(user));
        model.addAttribute("transactions", userService.getWalletTransactions(user));
        model.addAttribute("totalEarnings", userService.getTotalEarnings(user));
        model.addAttribute("totalWithdrawals", userService.getTotalWithdrawals(user));
        return "freelancer/wallet";
    }

    @PostMapping("/wallet/add-funds")
    public String addFunds(@RequestParam java.math.BigDecimal amount,
            @RequestParam String paymentMethod,
            Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            User user = getCurrentUser(auth);
            userService.addToWallet(user, amount, "Added funds via " + paymentMethod);
            redirectAttrs.addFlashAttribute("success", "₹" + amount + " added to your wallet!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/freelancer/wallet";
    }

    @PostMapping("/wallet/withdraw")
    public String withdraw(@RequestParam java.math.BigDecimal amount,
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
        return "redirect:/freelancer/wallet";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        return "freelancer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, Authentication auth,
            RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(auth);
        user.setFullName(updatedUser.getFullName());
        user.setBio(updatedUser.getBio());
        user.setSkills(updatedUser.getSkills());
        user.setLocation(updatedUser.getLocation());
        user.setPhone(updatedUser.getPhone());
        userService.save(user);
        redirectAttrs.addFlashAttribute("success", "Profile updated!");
        return "redirect:/freelancer/profile";
    }
}
