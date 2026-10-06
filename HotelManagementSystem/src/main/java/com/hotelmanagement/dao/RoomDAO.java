package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.RoomStatus;
import com.hotelmanagement.model.RoomType;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch rooms", e);
        }
        return list;
    }

    public Room findById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch room by ID: " + roomId, e);
        }
        return null;
    }

    public Room findByRoomNumber(String roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch room number: " + roomNumber, e);
        }
        return null;
    }

    public List<Room> search(String roomNumber, String type, String status, Integer floor) {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM rooms WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (roomNumber != null && !roomNumber.trim().isEmpty()) {
            sql.append("AND room_number LIKE ? ");
            params.add("%" + roomNumber.trim() + "%");
        }
        if (type != null && !type.equalsIgnoreCase("All")) {
            sql.append("AND room_type = ? ");
            params.add(type);
        }
        if (status != null && !status.equalsIgnoreCase("All")) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        if (floor != null && floor > 0) {
            sql.append("AND floor = ? ");
            params.add(floor);
        }
        sql.append("ORDER BY room_number ASC");

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
            throw new DatabaseException("Failed to search rooms", e);
        }
        return list;
    }

    /**
     * Dedicated Room Availability Query:
     * Finds rooms not in Maintenance, with capacity >= required guests,
     * matching optional RoomType and floor, AND with NO overlapping reservations
     * between checkInDate and checkOutDate.
     */
    public List<Room> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate,
                                         RoomType roomType, int numberOfGuests, Integer floor) {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.* FROM rooms r ");
        sql.append("WHERE r.status != 'Maintenance' ");
        sql.append("AND r.capacity >= ? ");

        List<Object> params = new ArrayList<>();
        params.add(numberOfGuests);

        if (roomType != null) {
            sql.append("AND r.room_type = ? ");
            params.add(roomType.name());
        }
        if (floor != null && floor > 0) {
            sql.append("AND r.floor = ? ");
            params.add(floor);
        }

        // Room is NOT available if there exists an active reservation with overlapping dates
        // (res.check_in_date < ? AND res.check_out_date > ?)
        sql.append("AND r.room_id NOT IN (");
        sql.append("  SELECT res.room_id FROM reservations res ");
        sql.append("  WHERE res.status IN ('Confirmed', 'Checked-In') ");
        sql.append("  AND res.check_in_date < ? ");
        sql.append("  AND res.check_out_date > ? ");
        sql.append(") ");
        sql.append("ORDER BY r.room_number ASC");

        params.add(Date.valueOf(checkOutDate));
        params.add(Date.valueOf(checkInDate));

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
            throw new DatabaseException("Failed to query available rooms", e);
        }
        return list;
    }

    public int create(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, floor, price_per_night, capacity, status, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getFloor());
            ps.setBigDecimal(4, room.getPricePerNight());
            ps.setInt(5, room.getCapacity());
            ps.setString(6, room.getStatus().name());
            ps.setString(7, room.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    room.setRoomId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert room: " + room.getRoomNumber(), e);
        }
        return 0;
    }

    public void update(Room room) {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, floor = ?, price_per_night = ?, capacity = ?, status = ?, description = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getFloor());
            ps.setBigDecimal(4, room.getPricePerNight());
            ps.setInt(5, room.getCapacity());
            ps.setString(6, room.getStatus().name());
            ps.setString(7, room.getDescription());
            ps.setInt(8, room.getRoomId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room ID: " + room.getRoomId(), e);
        }
    }

    public void updateStatus(int roomId, RoomStatus status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room status for room ID: " + roomId, e);
        }
    }

    public void updateStatus(Connection conn, int roomId, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            ps.executeUpdate();
        }
    }

    public void delete(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete room ID " + roomId + ". Room has historical bookings.", e);
        }
    }

    private Room mapResultSet(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setFloor(rs.getInt("floor"));
        r.setPricePerNight(rs.getBigDecimal("price_per_night"));
        r.setCapacity(rs.getInt("capacity"));
        r.setStatus(RoomStatus.valueOf(rs.getString("status")));
        r.setDescription(rs.getString("description"));
        return r;
    }
}
