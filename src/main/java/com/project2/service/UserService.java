package com.project2.service;

import com.project2.model.*;
import com.project2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, WalletTransactionRepository walletTransactionRepository,
            WithdrawalRepository withdrawalRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getWalletBalance() == null) {
            user.setWalletBalance(BigDecimal.ZERO);
        }
        if (user.getPerformanceScore() == null) {
            user.setPerformanceScore(5.0);
        }
        user.setIsVerified(false);
        user.setIsActive(false); // Only activate after verification

        // Generate 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));

        User savedUser = userRepository.save(user);
        try {
            emailService.sendOtpEmail(savedUser.getEmail(), otp);
        } catch (Exception e) {
            // Rethrow to be caught by AuthController
            throw new RuntimeException("Account saved but failed to send verification email: " + e.getMessage());
        }
        return savedUser;
    }

    @Transactional
    public boolean verifyOtp(String username, String otpCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpCode() != null && user.getOtpCode().equals(otpCode) &&
                user.getOtpExpiry().isAfter(java.time.LocalDateTime.now())) {

            user.setIsVerified(true);
            user.setIsActive(true);
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);

            // Send successful registration emails now
            try {
                emailService.sendWelcomeEmail(user);
                emailService.sendAdminRegistrationNotification(user);
            } catch (Exception e) {
                System.err.println("Failed to send post-verification emails: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void addToWallet(User user, BigDecimal amount, String description) {
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        WalletTransaction tx = new WalletTransaction(user, amount, "CREDIT", description);
        walletTransactionRepository.save(tx);
    }

    @Transactional
    public void deductFromWallet(User user, BigDecimal amount, String description) {
        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        userRepository.save(user);

        WalletTransaction tx = new WalletTransaction(user, amount, "DEBIT", description);
        walletTransactionRepository.save(tx);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public List<User> findFreelancersByCategory(String category) {
        return userRepository.findByRoleAndIsActiveAndCategoryContainingIgnoreCase(Role.FREELANCER, true, category);
    }

    @Transactional
    public void requestWithdrawal(User user, BigDecimal amount, String accName, String accNum, String ifsc,
            String bank) {
        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance for withdrawal");
        }

        // Deduct balance immediately
        deductFromWallet(user, amount, "Withdrawal request to " + bank + " (" + accNum + ")");

        // Save withdrawal request
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(amount);
        withdrawal.setAccountName(accName);
        withdrawal.setAccountNumber(accNum);
        withdrawal.setIfscCode(ifsc);
        withdrawal.setBankName(bank);
        withdrawal.setStatus("PENDING");
        withdrawalRepository.save(withdrawal);
    }

    public List<Withdrawal> findWithdrawalsByUser(User user) {
        return withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long countByRole(Role role) {
        return userRepository.findByRole(role).size();
    }

    public BigDecimal getTotalEarnings(User user) {
        return walletTransactionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(tx -> "CREDIT".equals(tx.getType()))
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalWithdrawals(User user) {
        return withdrawalRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(w -> "APPROVED".equals(w.getStatus()))
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<WalletTransaction> getWalletTransactions(User user) {
        return walletTransactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public String createPasswordResetOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setResetToken(otp); // Using resetToken field for OTP
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user, otp);
        return otp;
    }

    public boolean verifyPasswordResetOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getResetToken() != null &&
               user.getResetToken().equals(otp) &&
               user.getResetTokenExpiry().isAfter(java.time.LocalDateTime.now());
    }

    public Optional<User> validatePasswordResetToken(String token) {
        return userRepository.findByResetToken(token)
                .filter(user -> user.getResetTokenExpiry().isAfter(java.time.LocalDateTime.now()));
    }

    @Transactional
    public void resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void updateProfilePicture(User user, String base64Image) {
        user.setProfilePicture(base64Image);
        userRepository.save(user);
    }
}
