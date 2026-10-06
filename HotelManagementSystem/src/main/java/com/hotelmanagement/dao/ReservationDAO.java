package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.RoomType;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private static final String BASE_SELECT =
            "SELECT res.*, g.full_name AS guest_name, g.phone AS guest_phone, g.email AS guest_email, " +
            "r.room_number, r.room_type, r.price_per_night AS room_price " +
            "FROM reservations res " +
            "JOIN guests g ON res.guest_id = g.guest_id " +
            "JOIN rooms r ON res.room_id = r.room_id ";

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY res.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch reservations", e);
        }
        return list;
    }

    public Reservation findById(int reservationId) {
        String sql = BASE_SELECT + "WHERE res.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch reservation ID: " + reservationId, e);
        }
        return null;
    }

    public List<Reservation> findByGuestId(int guestId) {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE res.guest_id = ? ORDER BY res.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch reservations for guest ID: " + guestId, e);
        }
        return list;
    }

    public List<Reservation> search(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (g.full_name LIKE ? OR r.room_number LIKE ? OR g.phone LIKE ? OR CAST(res.reservation_id AS CHAR) = ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term); params.add(term); params.add(term); params.add(keyword.trim());
        }
        if (status != null && !status.equalsIgnoreCase("All")) {
            sql.append("AND res.status = ? ");
            params.add(status);
        }
        if (fromDate != null) {
            sql.append("AND res.check_in_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND res.check_out_date <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY res.reservation_id DESC");

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
            throw new DatabaseException("Failed to search reservations", e);
        }
        return list;
    }

    /**
     * Checks if a room has any overlapping active reservation (Confirmed or Checked-In).
     */
    public boolean hasOverlap(Connection conn, int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeReservationId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM reservations WHERE room_id = ? " +
                "AND status IN ('Confirmed', 'Checked-In') " +
                "AND check_in_date < ? AND check_out_date > ? "
        );
        if (excludeReservationId != null) {
            sql.append("AND reservation_id != ? ");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (excludeReservationId != null) {
                ps.setInt(4, excludeReservationId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int create(Connection conn, Reservation res) throws SQLException {
        String sql = "INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests, created_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, res.getGuestId());
            ps.setInt(2, res.getRoomId());
            ps.setDate(3, Date.valueOf(res.getCheckInDate()));
            ps.setDate(4, Date.valueOf(res.getCheckOutDate()));
            ps.setInt(5, res.getNumberOfGuests());
            ps.setString(6, res.getStatus().toString());
            ps.setString(7, res.getSpecialRequests());
            if (res.getCreatedByUserId() != null) ps.setInt(8, res.getCreatedByUserId()); else ps.setNull(8, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    res.setReservationId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void update(Connection conn, Reservation res) throws SQLException {
        String sql = "UPDATE reservations SET guest_id = ?, room_id = ?, check_in_date = ?, check_out_date = ?, number_of_guests = ?, status = ?, special_requests = ? WHERE reservation_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, res.getGuestId());
            ps.setInt(2, res.getRoomId());
            ps.setDate(3, Date.valueOf(res.getCheckInDate()));
            ps.setDate(4, Date.valueOf(res.getCheckOutDate()));
            ps.setInt(5, res.getNumberOfGuests());
            ps.setString(6, res.getStatus().toString());
            ps.setString(7, res.getSpecialRequests());
            ps.setInt(8, res.getReservationId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection conn, int reservationId, ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toString());
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int reservationId, ReservationStatus status) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            updateStatus(conn, reservationId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update status for reservation ID: " + reservationId, e);
        }
    }

    public void delete(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete reservation ID " + reservationId + " with existing check-in or billing records.", e);
        }
    }

    private Reservation mapResultSet(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setGuestId(rs.getInt("guest_id"));
        r.setRoomId(rs.getInt("room_id"));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setNumberOfGuests(rs.getInt("number_of_guests"));
        Timestamp resTs = rs.getTimestamp("reservation_date");
        if (resTs != null) r.setReservationDate(resTs.toLocalDateTime());
        r.setStatus(ReservationStatus.fromString(rs.getString("status")));
        r.setSpecialRequests(rs.getString("special_requests"));
        int uid = rs.getInt("created_by_user_id");
        if (!rs.wasNull()) r.setCreatedByUserId(uid);

        // Joined fields
        r.setGuestName(rs.getString("guest_name"));
        r.setGuestPhone(rs.getString("guest_phone"));
        r.setGuestEmail(rs.getString("guest_email"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setRoomPrice(rs.getBigDecimal("room_price"));
        return r;
    }
}
