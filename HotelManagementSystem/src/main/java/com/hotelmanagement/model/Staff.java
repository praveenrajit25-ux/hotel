package com.hotelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Staff {
    private Integer staffId;
    private Integer userId;
    private String employeeCode;
    private String fullName;
    private UserRole role;
    private String phone;
    private String email;
    private String address;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private String status; // Active, Inactive, Terminated
    private String username; // from joined users table
    private LocalDateTime createdAt;

    public Staff() {}

    public Staff(Integer staffId, Integer userId, String employeeCode, String fullName, UserRole role,
                 String phone, String email, String address, BigDecimal salary, LocalDate joiningDate, String status) {
        this.staffId = staffId;
        this.userId = userId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.salary = salary;
        this.joiningDate = joiningDate;
        this.status = status;
    }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + " [" + employeeCode + " - " + role + "]";
    }
}
