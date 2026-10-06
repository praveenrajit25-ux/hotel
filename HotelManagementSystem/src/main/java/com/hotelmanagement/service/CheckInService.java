package com.hotelmanagement.service;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.dao.CheckInDAO;
import com.hotelmanagement.dao.ReservationDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.exception.ReservationException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.CheckIn;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.RoomStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CheckInService {

    private final CheckInDAO checkInDAO;
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;

    public CheckInService() {
        this.checkInDAO = new CheckInDAO();
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
    }

    public List<CheckIn> getAllCheckIns() {
        return checkInDAO.findAll();
    }

    public CheckIn getCheckInByReservationId(int reservationId) {
        return checkInDAO.findByReservationId(reservationId);
    }

    /**
     * Performs Check-in atomically: verifies reservation status, creates check_in record,
     * updates reservation status to Checked-In, and marks room as Occupied.
     */
    public int processCheckIn(int reservationId, String roomKey, boolean verifiedId, String notes, Integer staffUserId) {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new ReservationException("Reservation #" + reservationId + " not found.");
        }

        if (res.getStatus() == ReservationStatus.Checked_In) {
            throw new ReservationException("Reservation #" + reservationId + " is already checked in.");
        }
        if (res.getStatus() == ReservationStatus.Checked_Out) {
            throw new ReservationException("Reservation #" + reservationId + " is already checked out.");
        }
        if (res.getStatus() == ReservationStatus.Cancelled) {
            throw new ReservationException("Cannot check in a cancelled reservation.");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                CheckIn ci = new CheckIn();
                ci.setReservationId(reservationId);
                ci.setCheckInTime(LocalDateTime.now());
                ci.setRoomKeyIssued(roomKey != null && !roomKey.trim().isEmpty() ? roomKey.trim() : "Key Card #1");
                ci.setVerifiedIdProof(verifiedId);
                ci.setNotes(notes);
                ci.setProcessedByUserId(staffUserId);

                int checkInId = checkInDAO.create(conn, ci);

                // Update reservation to Checked-In
                reservationDAO.updateStatus(conn, reservationId, ReservationStatus.Checked_In);

                // Update room to Occupied
                roomDAO.updateStatus(conn, res.getRoomId(), RoomStatus.Occupied);

                conn.commit();
                return checkInId;
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Failed to process check-in: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during check-in transaction", e);
        }
    }
}
