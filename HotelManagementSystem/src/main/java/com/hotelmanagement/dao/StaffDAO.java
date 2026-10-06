package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Staff;
import com.hotelmanagement.model.UserRole;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public List<Staff> findAll() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM staff s LEFT JOIN users u ON s.user_id = u.user_id ORDER BY s.staff_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch staff list", e);
        }
        return list;
    }

    public Staff findById(int staffId) {
        String sql = "SELECT s.*, u.username FROM staff s LEFT JOIN users u ON s.user_id = u.user_id WHERE s.staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query staff ID: " + staffId, e);
        }
        return null;
    }

    public List<Staff> search(String keyword, String role, String status) {
        List<Staff> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, u.username FROM staff s LEFT JOIN users u ON s.user_id = u.user_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (s.full_name LIKE ? OR s.employee_code LIKE ? OR s.phone LIKE ? OR s.email LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term); params.add(term); params.add(term); params.add(term);
        }
        if (role != null && !role.equalsIgnoreCase("All")) {
            sql.append("AND s.role = ? ");
            params.add(role);
        }
        if (status != null && !status.equalsIgnoreCase("All")) {
            sql.append("AND s.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY s.staff_id ASC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search staff records", e);
        }
        return list;
    }

    public int create(Staff staff) {
        String sql = "INSERT INTO staff (user_id, employee_code, full_name, role, phone, email, address, salary, joining_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (staff.getUserId() != null) ps.setInt(1, staff.getUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, staff.getEmployeeCode());
            ps.setString(3, staff.getFullName());
            ps.setString(4, staff.getRole().name());
            ps.setString(5, staff.getPhone());
            ps.setString(6, staff.getEmail());
            ps.setString(7, staff.getAddress());
            ps.setBigDecimal(8, staff.getSalary());
            ps.setDate(9, Date.valueOf(staff.getJoiningDate()));
            ps.setString(10, staff.getStatus() != null ? staff.getStatus() : "Active");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    staff.setStaffId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert staff member: " + staff.getFullName(), e);
        }
        return 0;
    }

    public void update(Staff staff) {
        String sql = "UPDATE staff SET full_name = ?, role = ?, phone = ?, email = ?, address = ?, salary = ?, status = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getFullName());
            ps.setString(2, staff.getRole().name());
            ps.setString(3, staff.getPhone());
            ps.setString(4, staff.getEmail());
            ps.setString(5, staff.getAddress());
            ps.setBigDecimal(6, staff.getSalary());
            ps.setString(7, staff.getStatus());
            ps.setInt(8, staff.getStaffId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update staff ID: " + staff.getStaffId(), e);
        }
    }

    public void delete(int staffId) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete staff ID: " + staffId, e);
        }
    }

    private Staff mapResultSet(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) s.setUserId(uid);
        s.setEmployeeCode(rs.getString("employee_code"));
        s.setFullName(rs.getString("full_name"));
        s.setRole(UserRole.valueOf(rs.getString("role")));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setSalary(rs.getBigDecimal("salary"));
        Date jDate = rs.getDate("joining_date");
        if (jDate != null) s.setJoiningDate(jDate.toLocalDate());
        s.setStatus(rs.getString("status"));
        s.setUsername(rs.getString("username"));
        return s;
    }
}
