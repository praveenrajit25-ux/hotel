package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.DashboardStats;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    /**
     * Real dynamic calculations for Dashboard cards and summary metrics.
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // 1. Room Status Counts
            String roomSql = "SELECT status, COUNT(*) as cnt FROM rooms GROUP BY status";
            int totalRooms = 0;
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(roomSql)) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("cnt");
                    totalRooms += count;
                    switch (status) {
                        case "Available" -> stats.setAvailableRooms(count);
                        case "Reserved" -> stats.setReservedRooms(count);
                        case "Occupied" -> stats.setOccupiedRooms(count);
                        case "Cleaning" -> stats.setCleaningRooms(count);
                        case "Maintenance" -> stats.setMaintenanceRooms(count);
                    }
                }
            }
            stats.setTotalRooms(totalRooms);

            // 2. Total Registered Guests
            String guestSql = "SELECT COUNT(*) FROM guests";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(guestSql)) {
                if (rs.next()) stats.setTotalGuests(rs.getInt(1));
            }

            // 3. Today's Check-ins
            String todayCheckInSql = "SELECT COUNT(*) FROM check_ins WHERE DATE(check_in_time) = CURRENT_DATE";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(todayCheckInSql)) {
                if (rs.next()) stats.setTodayCheckIns(rs.getInt(1));
            }

            // 4. Today's Check-outs
            String todayCheckOutSql = "SELECT COUNT(*) FROM check_outs WHERE DATE(check_out_time) = CURRENT_DATE";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(todayCheckOutSql)) {
                if (rs.next()) stats.setTodayCheckOuts(rs.getInt(1));
            }

            // 5. Total Revenue (sum of all successful payments)
            String totalRevSql = "SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'Success'";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(totalRevSql)) {
                if (rs.next()) stats.setTotalRevenue(rs.getBigDecimal(1));
            }

            // 6. Today's Revenue
            String todayRevSql = "SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'Success' AND DATE(payment_date) = CURRENT_DATE";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(todayRevSql)) {
                if (rs.next()) stats.setTodayRevenue(rs.getBigDecimal(1));
            }

            // 7. Active Reservations (Pending, Confirmed, Checked-In)
            String activeResSql = "SELECT COUNT(*) FROM reservations WHERE status IN ('Pending', 'Confirmed', 'Checked-In')";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(activeResSql)) {
                if (rs.next()) stats.setActiveReservations(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate live dashboard statistics", e);
        }
        return stats;
    }

    /**
     * Daily Revenue Breakdown Report.
     */
    public List<Map<String, Object>> getDailyRevenueReport(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT DATE(p.payment_date) AS pay_date, p.payment_method, COUNT(p.payment_id) AS txn_count, " +
                     "SUM(p.amount) AS total_amount " +
                     "FROM payments p WHERE p.payment_status = 'Success' " +
                     "AND DATE(p.payment_date) BETWEEN ? AND ? " +
                     "GROUP BY DATE(p.payment_date), p.payment_method " +
                     "ORDER BY pay_date DESC, total_amount DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", rs.getDate("pay_date").toLocalDate());
                    row.put("method", rs.getString("payment_method"));
                    row.put("transactions", rs.getInt("txn_count"));
                    row.put("amount", rs.getBigDecimal("total_amount"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate Daily Revenue Report", e);
        }
        return list;
    }

    /**
     * Monthly Revenue Breakdown Report.
     */
    public List<Map<String, Object>> getMonthlyRevenueReport(int year) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS month_name, " +
                     "COUNT(payment_id) AS total_transactions, " +
                     "SUM(amount) AS total_amount " +
                     "FROM payments WHERE payment_status = 'Success' " +
                     "AND YEAR(payment_date) = ? " +
                     "GROUP BY DATE_FORMAT(payment_date, '%Y-%m') " +
                     "ORDER BY month_name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("month", rs.getString("month_name"));
                    row.put("transactions", rs.getInt("total_transactions"));
                    row.put("amount", rs.getBigDecimal("total_amount"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate Monthly Revenue Report", e);
        }
        return list;
    }

    /**
     * Service Revenue Breakdown Report.
     */
    public List<Map<String, Object>> getServiceRevenueReport(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.service_name, s.category, SUM(gs.quantity) AS total_qty, " +
                     "SUM(gs.total_price) AS total_revenue " +
                     "FROM guest_services gs " +
                     "JOIN services s ON gs.service_id = s.service_id " +
                     "WHERE DATE(gs.service_date) BETWEEN ? AND ? " +
                     "GROUP BY s.service_id, s.service_name, s.category " +
                     "ORDER BY total_revenue DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("serviceName", rs.getString("service_name"));
                    row.put("category", rs.getString("category"));
                    row.put("quantity", rs.getInt("total_qty"));
                    row.put("revenue", rs.getBigDecimal("total_revenue"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate Service Revenue Report", e);
        }
        return list;
    }

    /**
     * Room Occupancy Metrics.
     */
    public Map<String, Object> getOccupancySummary() {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) AS total_rooms, " +
                     "SUM(CASE WHEN status = 'Occupied' THEN 1 ELSE 0 END) AS occupied, " +
                     "SUM(CASE WHEN status = 'Reserved' THEN 1 ELSE 0 END) AS reserved, " +
                     "SUM(CASE WHEN status = 'Available' THEN 1 ELSE 0 END) AS available, " +
                     "SUM(CASE WHEN status = 'Cleaning' THEN 1 ELSE 0 END) AS cleaning, " +
                     "SUM(CASE WHEN status = 'Maintenance' THEN 1 ELSE 0 END) AS maintenance " +
                     "FROM rooms";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total_rooms");
                int occupied = rs.getInt("occupied");
                map.put("total", total);
                map.put("occupied", occupied);
                map.put("reserved", rs.getInt("reserved"));
                map.put("available", rs.getInt("available"));
                map.put("cleaning", rs.getInt("cleaning"));
                map.put("maintenance", rs.getInt("maintenance"));
                double rate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;
                map.put("occupancyRate", String.format("%.1f%%", rate));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate occupancy summary", e);
        }
        return map;
    }
}
