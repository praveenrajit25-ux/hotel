package com.hotelmanagement.service;

import com.hotelmanagement.dao.StaffDAO;
import com.hotelmanagement.dao.UserDAO;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.Staff;
import com.hotelmanagement.model.User;
import com.hotelmanagement.util.PasswordUtil;
import com.hotelmanagement.util.ValidationUtil;
import java.util.List;

public class StaffService {

    private final StaffDAO staffDAO;
    private final UserDAO userDAO;

    public StaffService() {
        this.staffDAO = new StaffDAO();
        this.userDAO = new UserDAO();
    }

    public List<Staff> getAllStaff() {
        return staffDAO.findAll();
    }

    public List<Staff> searchStaff(String keyword, String role, String status) {
        return staffDAO.search(keyword, role, status);
    }

    public Staff getStaffById(int staffId) {
        return staffDAO.findById(staffId);
    }

    public void createStaffWithAccount(Staff staff, String username, String plainPassword) {
        validateStaff(staff);
        if (!ValidationUtil.isNotEmpty(username)) {
            throw new ValidationException("Username is required for staff login.");
        }
        if (!ValidationUtil.isNotEmpty(plainPassword) || plainPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }

        User existingUser = userDAO.findByUsername(username.trim());
        if (existingUser != null) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }

        // Create User Account with BCrypt hash
        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        user.setRole(staff.getRole());
        user.setFullName(staff.getFullName());
        user.setEmail(staff.getEmail());
        user.setPhone(staff.getPhone());
        user.setStatus("Active");

        int userId = userDAO.create(user);
        staff.setUserId(userId);
        staffDAO.create(staff);
    }

    public void updateStaff(Staff staff) {
        validateStaff(staff);
        staffDAO.update(staff);

        // Synchronize linked user details if exists
        if (staff.getUserId() != null) {
            User user = userDAO.findById(staff.getUserId());
            if (user != null) {
                user.setFullName(staff.getFullName());
                user.setEmail(staff.getEmail());
                user.setPhone(staff.getPhone());
                user.setRole(staff.getRole());
                user.setStatus(staff.getStatus().equalsIgnoreCase("Terminated") ? "Inactive" : staff.getStatus());
                userDAO.update(user);
            }
        }
    }

    public void deleteStaff(int staffId) {
        Staff staff = staffDAO.findById(staffId);
        if (staff != null) {
            staffDAO.delete(staffId);
            if (staff.getUserId() != null) {
                userDAO.delete(staff.getUserId());
            }
        }
    }

    private void validateStaff(Staff staff) {
        if (staff == null) throw new ValidationException("Staff details cannot be null.");
        if (!ValidationUtil.isNotEmpty(staff.getFullName())) throw new ValidationException("Full name is required.");
        if (!ValidationUtil.isNotEmpty(staff.getEmployeeCode())) throw new ValidationException("Employee code is required.");
        if (!ValidationUtil.isValidEmail(staff.getEmail())) throw new ValidationException("Valid email address is required.");
        if (!ValidationUtil.isValidPhone(staff.getPhone())) throw new ValidationException("Valid phone number is required.");
        if (staff.getRole() == null) throw new ValidationException("Staff role is required.");
        if (staff.getJoiningDate() == null) throw new ValidationException("Joining date is required.");
    }
}
