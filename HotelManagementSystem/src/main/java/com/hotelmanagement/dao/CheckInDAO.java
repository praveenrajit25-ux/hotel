package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.CheckIn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckInDAO {

    public CheckIn findByReservationId(int reservationId) {
        String sql = "SELECT ci.*, g.full_name AS guest_name, r.room_number, u.full_name AS staff_name " +
                     "FROM check_ins ci " +
                     "JOIN reservations res ON ci.reservation_id = res.reservation_id " +
                     "JOIN guests g ON res.guest_id = g.guest_id " +
                     "JOIN rooms r ON res.room_id = r.room_id " +
                     "LEFT JOIN users u ON ci.processed_by_user_id = u.user_id " +
                     "WHERE ci.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find check-in record for reservation ID: " + reservationId, e);
        }
        return null;
    }

    public List<CheckIn> findAll() {
        List<CheckIn> list = new ArrayList<>();
        String sql = "SELECT ci.*, g.full_name AS guest_name, r.room_number, u.full_name AS staff_name " +
                     "FROM check_ins ci " +
                     "JOIN reservations res ON ci.reservation_id = res.reservation_id " +
                     "JOIN guests g ON res.guest_id = g.guest_id " +
                     "JOIN rooms r ON res.room_id = r.room_id " +
                     "LEFT JOIN users u ON ci.processed_by_user_id = u.user_id " +
                     "ORDER BY ci.check_in_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list check-ins", e);
        }
        return list;
    }

    public int create(Connection conn, CheckIn ci) throws SQLException {
        String sql = "INSERT INTO check_ins (reservation_id, check_in_time, room_key_issued, verified_id_proof, notes, processed_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ci.getReservationId());
            ps.setTimestamp(2, Timestamp.valueOf(ci.getCheckInTime()));
            ps.setString(3, ci.getRoomKeyIssued());
            ps.setBoolean(4, ci.isVerifiedIdProof());
            ps.setString(5, ci.getNotes());
            if (ci.getProcessedByUserId() != null) ps.setInt(6, ci.getProcessedByUserId()); else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ci.setCheckInId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    private CheckIn mapResultSet(ResultSet rs) throws SQLException {
        CheckIn ci = new CheckIn();
        ci.setCheckInId(rs.getInt("check_in_id"));
        ci.setReservationId(rs.getInt("reservation_id"));
        ci.setCheckInTime(rs.getTimestamp("check_in_time").toLocalDateTime());
        ci.setRoomKeyIssued(rs.getString("room_key_issued"));
        ci.setVerifiedIdProof(rs.getBoolean("verified_id_proof"));
        ci.setNotes(rs.getString("notes"));
        int uid = rs.getInt("processed_by_user_id");
        if (!rs.wasNull()) ci.setProcessedByUserId(uid);
        ci.setGuestName(rs.getString("guest_name"));
        ci.setRoomNumber(rs.getString("room_number"));
        ci.setProcessedByStaffName(rs.getString("staff_name"));
        return ci;
    }
}
