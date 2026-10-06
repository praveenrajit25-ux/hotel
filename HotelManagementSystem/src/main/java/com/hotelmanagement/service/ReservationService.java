package com.hotelmanagement.service;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.dao.GuestDAO;
import com.hotelmanagement.dao.ReservationDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.exception.ReservationException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.Guest;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.RoomStatus;
import com.hotelmanagement.util.ValidationUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final GuestDAO guestDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.guestDAO = new GuestDAO();
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.findAll();
    }

    public Reservation getReservationById(int reservationId) {
        return reservationDAO.findById(reservationId);
    }

    public List<Reservation> searchReservations(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        return reservationDAO.search(keyword, status, fromDate, toDate);
    }

    /**
     * Creates a reservation atomically using a database transaction with overlap prevention and room status update.
     */
    public int createReservation(Reservation res) {
        validateReservation(res);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Verify room capacity
                Room room = roomDAO.findById(res.getRoomId());
                if (room == null) {
                    throw new ReservationException("Selected room does not exist.");
                }
                if (room.getStatus() == RoomStatus.Maintenance) {
                    throw new ReservationException("Room " + room.getRoomNumber() + " is currently under Maintenance and cannot be booked.");
                }
                if (res.getNumberOfGuests() > room.getCapacity()) {
                    throw new ReservationException("Guest count (" + res.getNumberOfGuests() + ") exceeds room capacity (" + room.getCapacity() + ").");
                }

                // Verify no overlapping bookings
                boolean overlap = reservationDAO.hasOverlap(conn, res.getRoomId(), res.getCheckInDate(), res.getCheckOutDate(), null);
                if (overlap) {
                    throw new ReservationException("Room " + room.getRoomNumber() + " is already booked for the selected date range.");
                }

                if (res.getStatus() == null) {
                    res.setStatus(ReservationStatus.Confirmed);
                }

                int reservationId = reservationDAO.create(conn, res);

                // Update Room status to Reserved if starting immediately or currently available
                if (res.getStatus() == ReservationStatus.Confirmed && room.getStatus() == RoomStatus.Available) {
                    if (res.getCheckInDate().isEqual(LocalDate.now())) {
                        roomDAO.updateStatus(conn, res.getRoomId(), RoomStatus.Reserved);
                    }
                }

                conn.commit();
                return reservationId;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof ReservationException) throw (ReservationException) e;
                throw new DatabaseException("Failed to create reservation: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database transaction failure: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing reservation with atomic validation.
     */
    public void updateReservation(Reservation res) {
        validateReservation(res);
        if (res.getReservationId() == null || res.getReservationId() <= 0) {
            throw new ValidationException("Invalid Reservation ID.");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Room room = roomDAO.findById(res.getRoomId());
                if (room == null) throw new ReservationException("Room does not exist.");
                if (res.getNumberOfGuests() > room.getCapacity()) {
                    throw new ReservationException("Guest count exceeds room capacity of " + room.getCapacity());
                }

                boolean overlap = reservationDAO.hasOverlap(conn, res.getRoomId(), res.getCheckInDate(), res.getCheckOutDate(), res.getReservationId());
                if (overlap) {
                    throw new ReservationException("Room " + room.getRoomNumber() + " has conflicting bookings for these new dates.");
                }

                reservationDAO.update(conn, res);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof ReservationException) throw (ReservationException) e;
                throw new DatabaseException("Failed to update reservation: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during reservation update", e);
        }
    }

    /**
     * Cancels a reservation and releases the room.
     */
    public void cancelReservation(int reservationId) {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) throw new ReservationException("Reservation #" + reservationId + " not found.");

        if (res.getStatus() == ReservationStatus.Checked_In || res.getStatus() == ReservationStatus.Checked_Out) {
            throw new ReservationException("Cannot cancel a reservation that is already " + res.getStatus().getDisplayName());
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                reservationDAO.updateStatus(conn, reservationId, ReservationStatus.Cancelled);

                // If room was marked Reserved, release it back to Available
                Room room = roomDAO.findById(res.getRoomId());
                if (room != null && room.getStatus() == RoomStatus.Reserved) {
                    roomDAO.updateStatus(conn, room.getRoomId(), RoomStatus.Available);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Failed to cancel reservation", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during cancellation", e);
        }
    }

    private void validateReservation(Reservation res) {
        if (res == null) throw new ValidationException("Reservation details cannot be null.");
        if (res.getGuestId() == null || res.getGuestId() <= 0) throw new ValidationException("Please select a valid guest.");
        if (res.getRoomId() == null || res.getRoomId() <= 0) throw new ValidationException("Please select a valid room.");
        if (res.getCheckInDate() == null || res.getCheckOutDate() == null) {
            throw new ValidationException("Check-in and Check-out dates are required.");
        }
        if (!ValidationUtil.isValidDateRange(res.getCheckInDate(), res.getCheckOutDate())) {
            throw new ValidationException("Check-out date must be strictly after Check-in date.");
        }
        if (res.getNumberOfGuests() == null || res.getNumberOfGuests() <= 0) {
            throw new ValidationException("Number of guests must be at least 1.");
        }
    }
}
