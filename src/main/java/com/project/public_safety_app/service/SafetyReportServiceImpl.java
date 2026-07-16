package com.project.public_safety_app.service;

import com.project.public_safety_app.model.SafetyReport;
import com.project.public_safety_app.model.User;
import com.project.public_safety_app.repository.SafetyReportRepository;
import com.project.public_safety_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SafetyReportServiceImpl implements SafetyReportService {

    @Autowired
    private SafetyReportRepository safetyReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private static void validateUserName(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (!USERNAME_PATTERN.matcher(userName).matches()) {
            throw new IllegalArgumentException("Username must be alphanumeric and between 3 to 30 characters");
        }
    }

    public SafetyReport createReport(SafetyReport report, User user) {

        // Check if user already exists
        User existingUser = userRepository.findByUserName(user.getUserName());
        if (existingUser == null) {
            // Hash the password before saving the user
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userRepository.save(user);
        } else {
            user = existingUser; // Use the existing user
        }

        // Set the user in the report
        report.setUser(user);

        // Save the report to ensure it has a valid ID
        SafetyReport savedReport = safetyReportRepository.save(report);

        // Manage the bidirectional relationship
        if (user.getSafetyReports() == null) {
            user.setSafetyReports(new ArrayList<>());
        }
        user.getSafetyReports().add(savedReport); // Add the saved report to the user's list

        // Save the user to maintain the relationship
        userRepository.save(user);

        return savedReport;
    }

    public List<SafetyReport> getAllReports() {
        return safetyReportRepository.findAll();
    }

    public List<SafetyReport> getReportsByUser(String userName) {
        validateUserName(userName);
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            return Collections.emptyList(); // Return an empty list if user is not found
        }
        return user.getSafetyReports();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @safetyReportServiceImpl.isReportOwner(#id, authentication.name))")
    public void deleteReport(Long id) {
        safetyReportRepository.deleteById(id);
    }

    public List<SafetyReport> getSafetyReportsBYUserName(String userName) {
        validateUserName(userName);
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            return Collections.emptyList(); // Return an empty list if user is not found
        }
        return user.getSafetyReports();
    }

    public boolean isReportOwner(Long reportId, String userName) {
        SafetyReport report = safetyReportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return false;
        }
        return report.getUser().getUserName().equals(userName);
    }
}