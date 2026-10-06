package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.CheckOut;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckOutDAO {

    public CheckOut findByReservationId(int reservationId) {
        String sql = "SELECT co.*, g.full_name AS guest_name, r.room_number, u.full_name AS staff_name " +
                     "FROM check_outs co " +
                     "JOIN reservations res ON co.reservation_id = res.reservation_id " +
                     "JOIN guests g ON res.guest_id = g.guest_id " +
                     "JOIN rooms r ON res.room_id = r.room_id " +
                     "LEFT JOIN users u ON co.processed_by_user_id = u.user_id " +
                     "WHERE co.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query checkout for reservation ID: " + reservationId, e);
        }
        return null;
    }

    public List<CheckOut> findAll() {
        List<CheckOut> list = new ArrayList<>();
        String sql = "SELECT co.*, g.full_name AS guest_name, r.room_number, u.full_name AS staff_name " +
                     "FROM check_outs co " +
                     "JOIN reservations res ON co.reservation_id = res.reservation_id " +
                     "JOIN guests g ON res.guest_id = g.guest_id " +
                     "JOIN rooms r ON res.room_id = r.room_id " +
                     "LEFT JOIN users u ON co.processed_by_user_id = u.user_id " +
                     "ORDER BY co.check_out_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list checkouts", e);
        }
        return list;
    }

    public int create(Connection conn, CheckOut co) throws SQLException {
        String sql = "INSERT INTO check_outs (reservation_id, check_out_time, room_inspected, key_returned, notes, processed_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, co.getReservationId());
            ps.setTimestamp(2, Timestamp.valueOf(co.getCheckOutTime()));
            ps.setBoolean(3, co.isRoomInspected());
            ps.setBoolean(4, co.isKeyReturned());
            ps.setString(5, co.getNotes());
            if (co.getProcessedByUserId() != null) ps.setInt(6, co.getProcessedByUserId()); else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    co.setCheckOutId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    private CheckOut mapResultSet(ResultSet rs) throws SQLException {
        CheckOut co = new CheckOut();
        co.setCheckOutId(rs.getInt("check_out_id"));
        co.setReservationId(rs.getInt("reservation_id"));
        co.setCheckOutTime(rs.getTimestamp("check_out_time").toLocalDateTime());
        co.setRoomInspected(rs.getBoolean("room_inspected"));
        co.setKeyReturned(rs.getBoolean("key_returned"));
        co.setNotes(rs.getString("notes"));
        int uid = rs.getInt("processed_by_user_id");
        if (!rs.wasNull()) co.setProcessedByUserId(uid);
        co.setGuestName(rs.getString("guest_name"));
        co.setRoomNumber(rs.getString("room_number"));
        co.setProcessedByStaffName(rs.getString("staff_name"));
        return co;
    }
}
