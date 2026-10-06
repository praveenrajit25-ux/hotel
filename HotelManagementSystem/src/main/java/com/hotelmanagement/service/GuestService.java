package com.hotelmanagement.service;

import com.hotelmanagement.dao.GuestDAO;
import com.hotelmanagement.dao.ReservationDAO;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.Guest;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.util.ValidationUtil;
import java.util.List;

public class GuestService {

    private final GuestDAO guestDAO;
    private final ReservationDAO reservationDAO;

    public GuestService() {
        this.guestDAO = new GuestDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public List<Guest> getAllGuests() {
        return guestDAO.findAll();
    }

    public Guest getGuestById(int guestId) {
        return guestDAO.findById(guestId);
    }

    public List<Guest> searchGuests(String keyword) {
        if (!ValidationUtil.isNotEmpty(keyword)) {
            return guestDAO.findAll();
        }
        return guestDAO.search(keyword);
    }

    public int registerGuest(Guest guest) {
        validateGuest(guest);
        return guestDAO.create(guest);
    }

    public void updateGuest(Guest guest) {
        validateGuest(guest);
        if (guest.getGuestId() == null || guest.getGuestId() <= 0) {
            throw new ValidationException("Invalid Guest ID.");
        }
        guestDAO.update(guest);
    }

    public void deleteGuest(int guestId) {
        List<Reservation> history = reservationDAO.findByGuestId(guestId);
        if (!history.isEmpty()) {
            throw new ValidationException("Cannot delete guest ID " + guestId + " because " + history.size() + " active/historical reservation records exist for this guest.");
        }
        guestDAO.delete(guestId);
    }

    public List<Reservation> getGuestReservationHistory(int guestId) {
        return reservationDAO.findByGuestId(guestId);
    }

    private void validateGuest(Guest guest) {
        if (guest == null) throw new ValidationException("Guest information cannot be null.");
        if (!ValidationUtil.isNotEmpty(guest.getFullName())) throw new ValidationException("Full name is required.");
        if (!ValidationUtil.isValidPhone(guest.getPhone())) throw new ValidationException("A valid contact phone number is required.");
        if (!ValidationUtil.isValidEmail(guest.getEmail())) throw new ValidationException("A valid email address is required.");
        if (guest.getIdProofType() == null) throw new ValidationException("ID proof type is required.");
        if (!ValidationUtil.isNotEmpty(guest.getIdProofNumber())) throw new ValidationException("ID proof number is required.");
    }
}
