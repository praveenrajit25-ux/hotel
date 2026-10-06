package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Guest;
import com.hotelmanagement.model.IdProofType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    public List<Guest> findAll() {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests ORDER BY guest_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch guest list", e);
        }
        return list;
    }

    public Guest findById(int guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query guest by ID: " + guestId, e);
        }
        return null;
    }

    public List<Guest> search(String keyword) {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE full_name LIKE ? OR phone LIKE ? OR email LIKE ? OR id_proof_number LIKE ? OR guest_id LIKE ? ORDER BY guest_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String term = "%" + keyword.trim() + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);
            ps.setString(4, term);
            ps.setString(5, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search guests with term: " + keyword, e);
        }
        return list;
    }

    public int create(Guest guest) {
        String sql = "INSERT INTO guests (full_name, gender, date_of_birth, phone, email, address, id_proof_type, id_proof_number, nationality) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getGender());
            if (guest.getDateOfBirth() != null) ps.setDate(3, Date.valueOf(guest.getDateOfBirth())); else ps.setNull(3, Types.DATE);
            ps.setString(4, guest.getPhone());
            ps.setString(5, guest.getEmail());
            ps.setString(6, guest.getAddress());
            ps.setString(7, guest.getIdProofType().name());
            ps.setString(8, guest.getIdProofNumber());
            ps.setString(9, guest.getNationality());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    guest.setGuestId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create guest record: " + guest.getFullName(), e);
        }
        return 0;
    }

    public void update(Guest guest) {
        String sql = "UPDATE guests SET full_name = ?, gender = ?, date_of_birth = ?, phone = ?, email = ?, address = ?, id_proof_type = ?, id_proof_number = ?, nationality = ? WHERE guest_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getGender());
            if (guest.getDateOfBirth() != null) ps.setDate(3, Date.valueOf(guest.getDateOfBirth())); else ps.setNull(3, Types.DATE);
            ps.setString(4, guest.getPhone());
            ps.setString(5, guest.getEmail());
            ps.setString(6, guest.getAddress());
            ps.setString(7, guest.getIdProofType().name());
            ps.setString(8, guest.getIdProofNumber());
            ps.setString(9, guest.getNationality());
            ps.setInt(10, guest.getGuestId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update guest ID: " + guest.getGuestId(), e);
        }
    }

    public void delete(int guestId) {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete guest ID " + guestId + ". Guest has active or historical reservation records.", e);
        }
    }

    private Guest mapResultSet(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFullName(rs.getString("full_name"));
        g.setGender(rs.getString("gender"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) g.setDateOfBirth(dob.toLocalDate());
        g.setPhone(rs.getString("phone"));
        g.setEmail(rs.getString("email"));
        g.setAddress(rs.getString("address"));
        g.setIdProofType(IdProofType.valueOf(rs.getString("id_proof_type")));
        g.setIdProofNumber(rs.getString("id_proof_number"));
        g.setNationality(rs.getString("nationality"));
        Timestamp reg = rs.getTimestamp("registration_date");
        if (reg != null) g.setRegistrationDate(reg.toLocalDateTime());
        return g;
    }
}
