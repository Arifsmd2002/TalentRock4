package com.project2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoController {

    @GetMapping("/how-it-works")
    public String howItWorks(Model model) {
        model.addAttribute("title", "How It Works");
        return "info/how-it-works";
    }

    @GetMapping("/security")
    public String security(Model model) {
        model.addAttribute("title", "Security & Data Protection");
        return "info/security";
    }

    @GetMapping("/investors")
    public String investors(Model model) {
        model.addAttribute("title", "Investor Relations");
        return "info/general";
    }

    @GetMapping("/sitemap")
    public String sitemap(Model model) {
        model.addAttribute("title", "Sitemap");
        return "info/sitemap";
    }

    @GetMapping("/stories")
    public String stories(Model model) {
        model.addAttribute("title", "Success Stories");
        return "info/general";
    }

    @GetMapping("/news")
    public String news(Model model) {
        model.addAttribute("title", "Talentrock News");
        return "info/general";
    }

    @GetMapping("/team")
    public String team(Model model) {
        model.addAttribute("title", "Our Team");
        return "info/general";
    }

    @GetMapping("/awards")
    public String awards(Model model) {
        model.addAttribute("title", "Awards and Recognition");
        return "info/general";
    }

    @GetMapping("/careers")
    public String careers(Model model) {
        model.addAttribute("title", "Careers at Talentrock");
        return "info/general";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model) {
        model.addAttribute("title", "Privacy Policy");
        return "info/privacy";
    }

    @GetMapping("/terms-and-conditions")
    public String termsAndConditions(Model model) {
        model.addAttribute("title", "Terms and Conditions");
        return "info/terms";
    }

    @GetMapping("/copyright-policy")
    public String copyrightPolicy(Model model) {
        model.addAttribute("title", "Copyright Policy");
        return "info/general";
    }

    @GetMapping("/code-of-conduct")
    public String codeOfConduct(Model model) {
        model.addAttribute("title", "Code of Conduct");
        return "info/general";
    }

    @GetMapping("/fees-and-charges")
    public String feesAndCharges(Model model) {
        model.addAttribute("title", "Fees and Charges");
        return "info/fees";
    }
}
