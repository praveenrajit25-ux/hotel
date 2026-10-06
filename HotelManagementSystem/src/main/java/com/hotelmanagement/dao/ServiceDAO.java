package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> findAll() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY category ASC, service_name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch hotel services", e);
        }
        return list;
    }

    public List<Service> findActiveServices() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE status = 'Active' ORDER BY service_name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch active services", e);
        }
        return list;
    }

    public Service findById(int serviceId) {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query service ID: " + serviceId, e);
        }
        return null;
    }

    public int create(Service s) {
        String sql = "INSERT INTO services (service_name, category, description, price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getServiceName());
            ps.setString(2, s.getCategory());
            ps.setString(3, s.getDescription());
            ps.setBigDecimal(4, s.getPrice());
            ps.setString(5, s.getStatus() != null ? s.getStatus() : "Active");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    s.setServiceId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create service: " + s.getServiceName(), e);
        }
        return 0;
    }

    public void update(Service s) {
        String sql = "UPDATE services SET service_name = ?, category = ?, description = ?, price = ?, status = ? WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getServiceName());
            ps.setString(2, s.getCategory());
            ps.setString(3, s.getDescription());
            ps.setBigDecimal(4, s.getPrice());
            ps.setString(5, s.getStatus());
            ps.setInt(6, s.getServiceId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update service ID: " + s.getServiceId(), e);
        }
    }

    public void delete(int serviceId) {
        String sql = "DELETE FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete service ID " + serviceId + ". It is assigned to existing guest invoices.", e);
        }
    }

    private Service mapResultSet(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setServiceId(rs.getInt("service_id"));
        s.setServiceName(rs.getString("service_name"));
        s.setCategory(rs.getString("category"));
        s.setDescription(rs.getString("description"));
        s.setPrice(rs.getBigDecimal("price"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
