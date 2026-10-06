package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.GuestService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestServiceDAO {

    public List<GuestService> findByReservationId(int reservationId) {
        List<GuestService> list = new ArrayList<>();
        String sql = "SELECT gs.*, s.service_name, s.category, g.full_name AS guest_name, r.room_number " +
                     "FROM guest_services gs " +
                     "JOIN services s ON gs.service_id = s.service_id " +
                     "JOIN reservations res ON gs.reservation_id = res.reservation_id " +
                     "JOIN guests g ON res.guest_id = g.guest_id " +
                     "JOIN rooms r ON res.room_id = r.room_id " +
                     "WHERE gs.reservation_id = ? " +
                     "ORDER BY gs.service_date ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch guest services for reservation ID: " + reservationId, e);
        }
        return list;
    }

    public List<GuestService> findAll() {
        List<GuestService> list = new ArrayList<>();
        String sql = "SELECT gs.*, s.service_name, s.category, g.full_name AS guest_name, r.room_number " +
                     "FROM guest_services gs " +
                     "JOIN services s ON gs.service_id = s.service_id " +
                     "JOIN reservations res ON gs.reservation_id = res.reservation_id " +
                     "JOIN guests g ON res.guest_id = g.guest_id " +
                     "JOIN rooms r ON res.room_id = r.room_id " +
                     "ORDER BY gs.guest_service_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all guest services", e);
        }
        return list;
    }

    public int create(GuestService gs) {
        String sql = "INSERT INTO guest_services (reservation_id, service_id, quantity, unit_price, total_price, service_date, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, gs.getReservationId());
            ps.setInt(2, gs.getServiceId());
            ps.setInt(3, gs.getQuantity());
            ps.setBigDecimal(4, gs.getUnitPrice());
            ps.setBigDecimal(5, gs.getTotalPrice());
            ps.setTimestamp(6, Timestamp.valueOf(gs.getServiceDate()));
            ps.setString(7, gs.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    gs.setGuestServiceId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to assign service to reservation", e);
        }
        return 0;
    }

    public void delete(int guestServiceId) {
        String sql = "DELETE FROM guest_services WHERE guest_service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestServiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to remove service charge ID: " + guestServiceId, e);
        }
    }

    private GuestService mapResultSet(ResultSet rs) throws SQLException {
        GuestService gs = new GuestService();
        gs.setGuestServiceId(rs.getInt("guest_service_id"));
        gs.setReservationId(rs.getInt("reservation_id"));
        gs.setServiceId(rs.getInt("service_id"));
        gs.setQuantity(rs.getInt("quantity"));
        gs.setUnitPrice(rs.getBigDecimal("unit_price"));
        gs.setTotalPrice(rs.getBigDecimal("total_price"));
        gs.setServiceDate(rs.getTimestamp("service_date").toLocalDateTime());
        gs.setNotes(rs.getString("notes"));
        gs.setServiceName(rs.getString("service_name"));
        gs.setServiceCategory(rs.getString("category"));
        gs.setGuestName(rs.getString("guest_name"));
        gs.setRoomNumber(rs.getString("room_number"));
        return gs;
    }
}
