package com.project2.util;

import com.project2.model.Role;
import com.project2.model.User;
import com.project2.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) throws Exception {
                // Ensure Admin if not exists
                if (!userRepository.existsByUsername("admin")) {
                        User admin = new User();
                        admin.setUsername("admin");
                        admin.setEmail("admin@talentrock.com");
                        admin.setPassword(passwordEncoder.encode("admin123"));
                        admin.setRole(Role.ADMIN);
                        admin.setFullName("Platform Admin");
                        admin.setIsVerified(true);
                        admin.setIsActive(true);
                        userRepository.save(admin);
                }

                // Create Sample Client if not exists
                if (!userRepository.existsByUsername("client")) {
                        User client = new User();
                        client.setUsername("client");
                        client.setEmail("client@gmail.com");
                        client.setPassword(passwordEncoder.encode("password123"));
                        client.setRole(Role.CLIENT);
                        client.setFullName("John Client");
                        client.setWalletBalance(new BigDecimal("100000"));
                        userRepository.save(client);
                }

                List<String> categories = Arrays.asList(
                                "Web Development", "Mobile Development", "Data Science", "Cloud Computing",
                                "Cyber Security",
                                "AI and Machine Learning", "Software Testing", "DevOps Engineering",
                                "Full Stack Development",
                                "Lead Generation", "Telemarketing", "Business Development", "Sales Strategy",
                                "Account Management",
                                "Shopify Experts", "Marketplace Management", "Product Listing", "eCommerce Marketing",
                                "Inventory Management",
                                "Supply Chain", "Warehouse Management", "Freight Forwarding", "Last-mile Delivery",
                                "Logistics Analytics",
                                "Logo Design", "Brand Identity", "UI/UX Design", "Motion Graphics", "Illustration");

                // Specific Profiles from Image 2
                seedSpecificFreelancer("Elite Information Tech", "Web Development", 15, 4.9, 7611, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=elite");
                seedSpecificFreelancer("Attari Bros", "Web Development", 20, 4.8, 7269, true,
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=attari");
                seedSpecificFreelancer("Seabit Media", "Web Development", 20, 4.9, 7106, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=seabit");
                seedSpecificFreelancer("theDesignerz", "Web Development", 25, 4.9, 6713, true,
                                "https://api.dicebear.com/7.x/bottts/svg?seed=designers");
                seedSpecificFreelancer("Dezine Geek", "Web Development", 25, 4.9, 5929, true,
                                "https://api.dicebear.com/7.x/pixel-art/svg?seed=dezine");
                seedSpecificFreelancer("colorgraphicz", "Web Development", 10, 4.9, 5437, true,
                                "https://api.dicebear.com/7.x/shapes/svg?seed=color");
                seedSpecificFreelancer("Gaurav C.", "Web Development", 40, 4.9, 5194, true,
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=gaurav");
                seedSpecificFreelancer("Marjan A.", "Web Development", 130, 5.0, 4436, true,
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=marjan");

                // Sector: Sales
                seedSpecificFreelancer("SalesBoost Global", "Lead Generation", 35, 4.9, 520, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=salesboost");
                seedSpecificFreelancer("Lead Magnetics", "Lead Generation", 25, 4.7, 312, true,
                                "https://api.dicebear.com/7.x/shapes/svg?seed=magnetics");
                seedSpecificFreelancer("TeleTalk Experts", "Telemarketing", 12, 4.8, 890, true,
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=teletalk");

                // Sector: eCommerce
                seedSpecificFreelancer("Shopify Genies", "Shopify Experts", 45, 5.0, 120, true,
                                "https://api.dicebear.com/7.x/bottts/svg?seed=genies");
                seedSpecificFreelancer("Pixel Commerce", "Marketplace Management", 30, 4.9, 445, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=pixelcomm");
                seedSpecificFreelancer("StoreFront Pros", "Product Listing", 15, 4.6, 1250, true,
                                "https://api.dicebear.com/7.x/shapes/svg?seed=storefront");

                // Sector: Logistics
                seedSpecificFreelancer("Global Supply Solutions", "Supply Chain", 60, 4.9, 85, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=globalsupply");
                seedSpecificFreelancer("Route Masters", "Logistics Analytics", 55, 4.8, 142, true,
                                "https://api.dicebear.com/7.x/bottts/svg?seed=routemasters");
                seedSpecificFreelancer("WareHouse Wizards", "Warehouse Management", 25, 4.7, 310, true,
                                "https://api.dicebear.com/7.x/pixel-art/svg?seed=warehouse");

                // Sector: Graphic Designing
                seedSpecificFreelancer("Creative Canvas", "Logo Design", 40, 5.0, 620, true,
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=canvas");
                seedSpecificFreelancer("Brand Builders", "Brand Identity", 75, 4.9, 128, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=brandbuild");
                seedSpecificFreelancer("Visionary UX", "UI/UX Design", 50, 4.9, 215, true,
                                "https://api.dicebear.com/7.x/shapes/svg?seed=visionary");

                // Sector: Mobile Development
                seedSpecificFreelancer("App Innovators Ltd", "Mobile Development", 55, 4.9, 120, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=appinn");
                seedSpecificFreelancer("Swift Solutionz", "Mobile Development", 45, 4.8, 85, true,
                                "https://api.dicebear.com/7.x/bottts/svg?seed=swift");

                // Sector: Cloud Computing
                seedSpecificFreelancer("Cloud Architects", "Cloud Computing", 120, 5.0, 45, true,
                                "https://api.dicebear.com/7.x/identicon/svg?seed=cloudarch");

                Random random = new Random();
                for (int i = 1; i <= 500; i++) {
                        String cat = categories.get(random.nextInt(categories.size()));
                        String username = "f" + i + "_" + cat.toLowerCase().replaceAll("[^a-z]", "");
                        if (userRepository.existsByUsername(username))
                                continue;

                        User freelancer = new User();
                        freelancer.setUsername(username);
                        freelancer.setEmail(username + "@talentrock.dev");
                        freelancer.setPassword(passwordEncoder.encode("pass123"));
                        freelancer.setRole(Role.FREELANCER);

                        String firstName = Arrays
                                        .asList("Arjun", "Neha", "Rahul", "Priya", "Amit", "Sonal", "Vikram", "Anjali",
                                                        "Karan", "Meera", "Sanya", "Rohan")
                                        .get(random.nextInt(12));
                        String lastName = Arrays
                                        .asList("Sharma", "Verma", "Gupta", "Malhotra", "Singh", "Joshi", "Mehta",
                                                        "Reddy", "Patel", "Nair", "Iyer", "Kulkarni")
                                        .get(random.nextInt(12));

                        // Randomly decide if it's an agency/company name or person name
                        if (random.nextDouble() > 0.8) {
                                freelancer.setFullName(Arrays
                                                .asList("Elite ", "Global ", "Prime ", "Apex ", "Nova ", "Direct ")
                                                .get(random.nextInt(6)) + cat + " Services");
                        } else {
                                freelancer.setFullName(firstName + " " + lastName);
                        }

                        freelancer.setCategory(cat);
                        freelancer.setSkills(cat + ", Expert, Professional, Specialized");
                        freelancer.setLocation(
                                        Arrays.asList("Mumbai", "Bangalore", "Delhi", "Remote", "Global", "Hyderabad")
                                                        .get(random.nextInt(6)));
                        freelancer.setBio("High-performance specialist in " + cat
                                        + " with proven track record on Talentrock.");
                        freelancer.setHourlyRate(new BigDecimal(15 + random.nextInt(150)));
                        freelancer.setPerformanceScore(4.0 + (random.nextDouble() * 1.0));
                        freelancer.setCompletedProjects(5 + random.nextInt(2000));
                        freelancer.setIsVerified(random.nextDouble() > 0.4);

                        String avatarStyle = Arrays.asList("avataaars", "identicon", "bottts", "pixel-art", "initials")
                                        .get(random.nextInt(5));
                        freelancer.setProfilePicture(
                                        "https://api.dicebear.com/7.x/" + avatarStyle + "/svg?seed=" + username);

                        userRepository.save(freelancer);
                }

                System.out.println(">>> Sample Freelancers Seeded Successfully!");
        }

        private void seedSpecificFreelancer(String name, String cat, int rate, double score, int jobs, boolean verified,
                        String img) {
                String username = name.toLowerCase().replaceAll("[^a-z]", "_");
                if (userRepository.existsByUsername(username))
                        return;

                User f = new User();
                f.setUsername(username);
                f.setEmail(username + "@talentrock.com");
                f.setPassword(passwordEncoder.encode("pass123"));
                f.setRole(Role.FREELANCER);
                f.setFullName(name);
                f.setCategory(cat);
                f.setHourlyRate(new BigDecimal(rate));
                f.setPerformanceScore(score);
                f.setCompletedProjects(jobs);
                f.setIsVerified(verified);
                f.setProfilePicture(img);
                f.setBio("Professional " + cat + " expert providing top-tier services.");
                f.setSkills(cat + ", Expert");
                f.setLocation("Global");
                userRepository.save(f);
        }
}
