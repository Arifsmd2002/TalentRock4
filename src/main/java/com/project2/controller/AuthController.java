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
        System.out.println(">>> DEBUG: Serving registration page v2.0 from " + this.getClass().getName());
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
            return "redirect:/verify-otp?username=" + user.getUsername();
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("Authentication failed") || errorMessage.contains("Username and Password not accepted") || errorMessage.contains("verification email"))) {
                errorMessage = "Email delivery failed: Authentication with the mail server failed. \n\n" +
                             "IMPORTANT: If you are using Gmail, you MUST use an 'App Password', not your regular login password. \n" +
                             "Go to Google Account -> Security -> 2-Step Verification -> App Passwords to create one.";
            }
            redirectAttrs.addFlashAttribute("error", errorMessage);
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam String username,
            @RequestParam String otp,
            RedirectAttributes redirectAttrs) {
        try {
            if (userService.verifyOtp(username, otp)) {
                redirectAttrs.addFlashAttribute("message", "Registration successful! You can now login.");
                return "redirect:/login";
            } else {
                redirectAttrs.addFlashAttribute("error", "Invalid or expired OTP.");
                return "redirect:/verify-otp?username=" + username;
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("Authentication failed") || errorMessage.contains("Username and Password not accepted"))) {
                errorMessage = "Verification successful, but welcome email failed: Please verify your Gmail App Password in application.properties.";
            }
            redirectAttrs.addFlashAttribute("error", errorMessage);
            return "redirect:/verify-otp?username=" + username;
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttrs) {
        try {
            userService.createPasswordResetOtp(email);
            return "redirect:/verify-reset-otp?email=" + email;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("Authentication failed") || errorMessage.contains("Username and Password not accepted"))) {
                errorMessage = "Email delivery failed: Authentication with the mail server failed. Please check your SMTP username and password in application.properties. If using Gmail, make sure to use an App Password.";
            }
            redirectAttrs.addFlashAttribute("error", errorMessage);
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/verify-reset-otp")
    public String verifyResetOtpPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify-reset-otp";
    }

    @PostMapping("/verify-reset-otp")
    public String processVerifyResetOtp(@RequestParam String email,
                                      @RequestParam String otp,
                                      RedirectAttributes redirectAttrs) {
        if (userService.verifyPasswordResetOtp(email, otp)) {
            return "redirect:/reset-password?email=" + email + "&token=" + otp;
        } else {
            redirectAttrs.addFlashAttribute("error", "Invalid or expired OTP.");
            return "redirect:/verify-reset-otp?email=" + email;
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String email, @RequestParam String token, Model model) {
        if (userService.verifyPasswordResetOtp(email, token)) {
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        return "redirect:/login?error=invalid-token";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                     @RequestParam String token,
                                     @RequestParam String password,
                                     RedirectAttributes redirectAttrs) {
        if (userService.verifyPasswordResetOtp(email, token)) {
            User user = userService.validatePasswordResetToken(token).orElseThrow();
            userService.resetPassword(user, password);
            redirectAttrs.addFlashAttribute("message", "Password successfully reset. Please login.");
            return "redirect:/login";
        }
        return "redirect:/login?error=invalid-token";
    }
}
