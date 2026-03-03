package com.project2.controller;

import com.project2.model.*;
import com.project2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("error", "Invalid username or password.");
        if (logout != null)
            model.addAttribute("message", "You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
            @RequestParam String role,
            RedirectAttributes redirectAttrs) {
        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
            userService.register(user);
            redirectAttrs.addFlashAttribute("success", "Account created! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        // Implementation here would redirect to a password reset page
        return "auth/forgot-password";
    }
}
