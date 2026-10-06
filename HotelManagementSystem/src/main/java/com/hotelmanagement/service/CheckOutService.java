package com.hotelmanagement.service;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.dao.CheckOutDAO;
import com.hotelmanagement.dao.InvoiceDAO;
import com.hotelmanagement.dao.ReservationDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.exception.ReservationException;
import com.hotelmanagement.model.CheckOut;
import com.hotelmanagement.model.Invoice;
import com.hotelmanagement.model.PaymentStatus;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.RoomStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CheckOutService {

    private final CheckOutDAO checkOutDAO;
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final InvoiceDAO invoiceDAO;

    public CheckOutService() {
        this.checkOutDAO = new CheckOutDAO();
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    public List<CheckOut> getAllCheckOuts() {
        return checkOutDAO.findAll();
    }

    public CheckOut getCheckOutByReservationId(int reservationId) {
        return checkOutDAO.findByReservationId(reservationId);
    }

    /**
     * Completes Check-out atomically:
     * 1. Checks that the reservation is Checked-In.
     * 2. Verifies that the invoice is Paid (or balance is Zero).
     * 3. Creates check_out record.
     * 4. Updates reservation to Checked-Out.
     * 5. Sets room status to Cleaning.
     */
    public int processCheckOut(int reservationId, boolean roomInspected, boolean keyReturned, String notes, Integer staffUserId) {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new ReservationException("Reservation #" + reservationId + " not found.");
        }
        if (res.getStatus() != ReservationStatus.Checked_In) {
            throw new ReservationException("Reservation #" + reservationId + " cannot be checked out because its status is " + res.getStatus().getDisplayName());
        }

        // Verify that invoice exists and balance is paid
        Invoice invoice = invoiceDAO.findByReservationId(reservationId);
        if (invoice == null) {
            throw new ReservationException("No invoice found for Reservation #" + reservationId + ". Please generate the invoice before check-out.");
        }
        if (invoice.getBalanceAmount() != null && invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new ReservationException("Cannot check out guest with outstanding balance of $" + invoice.getBalanceAmount() + ". Please settle the payment first.");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                CheckOut co = new CheckOut();
                co.setReservationId(reservationId);
                co.setCheckOutTime(LocalDateTime.now());
                co.setRoomInspected(roomInspected);
                co.setKeyReturned(keyReturned);
                co.setNotes(notes);
                co.setProcessedByUserId(staffUserId);

                int checkOutId = checkOutDAO.create(conn, co);

                // Update reservation to Checked-Out
                reservationDAO.updateStatus(conn, reservationId, ReservationStatus.Checked_Out);

                // Mark room status to Cleaning
                roomDAO.updateStatus(conn, res.getRoomId(), RoomStatus.Cleaning);

                conn.commit();
                return checkOutId;
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Failed to complete check-out: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database transaction error during checkout", e);
        }
    }
}
