package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Payment;
import com.hotelmanagement.model.PaymentMethod;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    private static final String BASE_SELECT =
            "SELECT p.*, inv.invoice_number, g.full_name AS guest_name, r.room_number, u.full_name AS staff_name " +
            "FROM payments p " +
            "JOIN invoices inv ON p.invoice_id = inv.invoice_id " +
            "JOIN reservations res ON p.reservation_id = res.reservation_id " +
            "JOIN guests g ON res.guest_id = g.guest_id " +
            "JOIN rooms r ON res.room_id = r.room_id " +
            "LEFT JOIN users u ON p.processed_by_user_id = u.user_id ";

    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY p.payment_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch payments list", e);
        }
        return list;
    }

    public List<Payment> findByInvoiceId(int invoiceId) {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.invoice_id = ? ORDER BY p.payment_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query payments for invoice ID: " + invoiceId, e);
        }
        return list;
    }

    public List<Payment> search(String keyword, String method, LocalDate fromDate, LocalDate toDate) {
        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (inv.invoice_number LIKE ? OR g.full_name LIKE ? OR p.transaction_reference LIKE ? OR r.room_number LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term); params.add(term); params.add(term); params.add(term);
        }
        if (method != null && !method.equalsIgnoreCase("All")) {
            sql.append("AND p.payment_method = ? ");
            params.add(method);
        }
        if (fromDate != null) {
            sql.append("AND p.payment_date >= ? ");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            sql.append("AND p.payment_date <= ? ");
            params.add(Timestamp.valueOf(toDate.atTime(23, 59, 59)));
        }
        sql.append("ORDER BY p.payment_id DESC");

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
            throw new DatabaseException("Failed to search payments", e);
        }
        return list;
    }

    public int create(Connection conn, Payment pay) throws SQLException {
        String sql = "INSERT INTO payments (invoice_id, reservation_id, amount, payment_method, payment_status, transaction_reference, payment_date, notes, processed_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pay.getInvoiceId());
            ps.setInt(2, pay.getReservationId());
            ps.setBigDecimal(3, pay.getAmount());
            ps.setString(4, pay.getPaymentMethod().toString());
            ps.setString(5, pay.getPaymentStatus() != null ? pay.getPaymentStatus() : "Success");
            ps.setString(6, pay.getTransactionReference());
            ps.setTimestamp(7, Timestamp.valueOf(pay.getPaymentDate()));
            ps.setString(8, pay.getNotes());
            if (pay.getProcessedByUserId() != null) ps.setInt(9, pay.getProcessedByUserId()); else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    pay.setPaymentId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    private Payment mapResultSet(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setInvoiceId(rs.getInt("invoice_id"));
        p.setReservationId(rs.getInt("reservation_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
        p.setPaymentStatus(rs.getString("payment_status"));
        p.setTransactionReference(rs.getString("transaction_reference"));
        p.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        p.setNotes(rs.getString("notes"));
        int uid = rs.getInt("processed_by_user_id");
        if (!rs.wasNull()) p.setProcessedByUserId(uid);

        p.setInvoiceNumber(rs.getString("invoice_number"));
        p.setGuestName(rs.getString("guest_name"));
        p.setRoomNumber(rs.getString("room_number"));
        p.setProcessedByStaffName(rs.getString("staff_name"));
        return p;
    }
}
