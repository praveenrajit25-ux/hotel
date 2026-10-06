package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.Invoice;
import com.hotelmanagement.model.PaymentMethod;
import com.hotelmanagement.model.PaymentStatus;
import com.hotelmanagement.model.RoomType;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    private static final String BASE_SELECT =
            "SELECT inv.*, g.full_name AS guest_name, g.phone AS guest_phone, g.email AS guest_email, " +
            "r.room_number, r.room_type " +
            "FROM invoices inv " +
            "JOIN guests g ON inv.guest_id = g.guest_id " +
            "JOIN rooms r ON inv.room_id = r.room_id ";

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY inv.invoice_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch invoice list", e);
        }
        return list;
    }

    public Invoice findById(int invoiceId) {
        String sql = BASE_SELECT + "WHERE inv.invoice_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query invoice ID: " + invoiceId, e);
        }
        return null;
    }

    public Invoice findByReservationId(int reservationId) {
        String sql = BASE_SELECT + "WHERE inv.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query invoice for reservation ID: " + reservationId, e);
        }
        return null;
    }

    public Invoice findByInvoiceNumber(String invoiceNumber) {
        String sql = BASE_SELECT + "WHERE inv.invoice_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query invoice number: " + invoiceNumber, e);
        }
        return null;
    }

    public List<Invoice> search(String keyword, String paymentStatus, LocalDate fromDate, LocalDate toDate) {
        List<Invoice> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (inv.invoice_number LIKE ? OR g.full_name LIKE ? OR r.room_number LIKE ? OR CAST(inv.reservation_id AS CHAR) = ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term); params.add(term); params.add(term); params.add(keyword.trim());
        }
        if (paymentStatus != null && !paymentStatus.equalsIgnoreCase("All")) {
            sql.append("AND inv.payment_status = ? ");
            params.add(paymentStatus);
        }
        if (fromDate != null) {
            sql.append("AND inv.invoice_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND inv.invoice_date <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY inv.invoice_id DESC");

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
            throw new DatabaseException("Failed to search invoices", e);
        }
        return list;
    }

    public int create(Connection conn, Invoice inv) throws SQLException {
        String sql = "INSERT INTO invoices (" +
                     "invoice_number, reservation_id, guest_id, room_id, check_in_date, check_out_date, " +
                     "number_of_nights, room_charges, service_charges, additional_charges, subtotal, " +
                     "discount, tax_percentage, tax_amount, grand_total, paid_amount, balance_amount, " +
                     "payment_status, payment_method, invoice_date" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getInvoiceNumber());
            ps.setInt(2, inv.getReservationId());
            ps.setInt(3, inv.getGuestId());
            ps.setInt(4, inv.getRoomId());
            ps.setDate(5, Date.valueOf(inv.getCheckInDate()));
            ps.setDate(6, Date.valueOf(inv.getCheckOutDate()));
            ps.setInt(7, inv.getNumberOfNights());
            ps.setBigDecimal(8, inv.getRoomCharges());
            ps.setBigDecimal(9, inv.getServiceCharges());
            ps.setBigDecimal(10, inv.getAdditionalCharges());
            ps.setBigDecimal(11, inv.getSubtotal());
            ps.setBigDecimal(12, inv.getDiscount());
            ps.setBigDecimal(13, inv.getTaxPercentage());
            ps.setBigDecimal(14, inv.getTaxAmount());
            ps.setBigDecimal(15, inv.getGrandTotal());
            ps.setBigDecimal(16, inv.getPaidAmount());
            ps.setBigDecimal(17, inv.getBalanceAmount());
            ps.setString(18, inv.getPaymentStatus().toString());
            ps.setString(19, inv.getPaymentMethod().toString());
            ps.setDate(20, Date.valueOf(inv.getInvoiceDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    inv.setInvoiceId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void update(Connection conn, Invoice inv) throws SQLException {
        String sql = "UPDATE invoices SET " +
                     "room_charges = ?, service_charges = ?, additional_charges = ?, subtotal = ?, " +
                     "discount = ?, tax_percentage = ?, tax_amount = ?, grand_total = ?, " +
                     "paid_amount = ?, balance_amount = ?, payment_status = ?, payment_method = ? " +
                     "WHERE invoice_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, inv.getRoomCharges());
            ps.setBigDecimal(2, inv.getServiceCharges());
            ps.setBigDecimal(3, inv.getAdditionalCharges());
            ps.setBigDecimal(4, inv.getSubtotal());
            ps.setBigDecimal(5, inv.getDiscount());
            ps.setBigDecimal(6, inv.getTaxPercentage());
            ps.setBigDecimal(7, inv.getTaxAmount());
            ps.setBigDecimal(8, inv.getGrandTotal());
            ps.setBigDecimal(9, inv.getPaidAmount());
            ps.setBigDecimal(10, inv.getBalanceAmount());
            ps.setString(11, inv.getPaymentStatus().toString());
            ps.setString(12, inv.getPaymentMethod().toString());
            ps.setInt(13, inv.getInvoiceId());
            ps.executeUpdate();
        }
    }

    public void updatePaymentStatusAndAmounts(Connection conn, int invoiceId, BigDecimal paidAmount, BigDecimal balanceAmount, PaymentStatus status) throws SQLException {
        String sql = "UPDATE invoices SET paid_amount = ?, balance_amount = ?, payment_status = ? WHERE invoice_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, paidAmount);
            ps.setBigDecimal(2, balanceAmount);
            ps.setString(3, status.toString());
            ps.setInt(4, invoiceId);
            ps.executeUpdate();
        }
    }

    private Invoice mapResultSet(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setReservationId(rs.getInt("reservation_id"));
        inv.setGuestId(rs.getInt("guest_id"));
        inv.setRoomId(rs.getInt("room_id"));
        inv.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        inv.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        inv.setNumberOfNights(rs.getInt("number_of_nights"));
        inv.setRoomCharges(rs.getBigDecimal("room_charges"));
        inv.setServiceCharges(rs.getBigDecimal("service_charges"));
        inv.setAdditionalCharges(rs.getBigDecimal("additional_charges"));
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setDiscount(rs.getBigDecimal("discount"));
        inv.setTaxPercentage(rs.getBigDecimal("tax_percentage"));
        inv.setTaxAmount(rs.getBigDecimal("tax_amount"));
        inv.setGrandTotal(rs.getBigDecimal("grand_total"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setBalanceAmount(rs.getBigDecimal("balance_amount"));
        inv.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
        inv.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
        inv.setInvoiceDate(rs.getDate("invoice_date").toLocalDate());

        inv.setGuestName(rs.getString("guest_name"));
        inv.setGuestPhone(rs.getString("guest_phone"));
        inv.setGuestEmail(rs.getString("guest_email"));
        inv.setRoomNumber(rs.getString("room_number"));
        inv.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        return inv;
    }
}
