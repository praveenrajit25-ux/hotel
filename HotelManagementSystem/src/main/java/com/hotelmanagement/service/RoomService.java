package com.hotelmanagement.service;

import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.RoomStatus;
import com.hotelmanagement.model.RoomType;
import com.hotelmanagement.util.ValidationUtil;
import java.time.LocalDate;
import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    public List<Room> getAllRooms() {
        return roomDAO.findAll();
    }

    public Room getRoomById(int roomId) {
        return roomDAO.findById(roomId);
    }

    public Room getRoomByNumber(String roomNumber) {
        return roomDAO.findByRoomNumber(roomNumber);
    }

    public List<Room> searchRooms(String roomNumber, String type, String status, Integer floor) {
        return roomDAO.search(roomNumber, type, status, floor);
    }

    public List<Room> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate,
                                         RoomType roomType, int numberOfGuests, Integer floor) {
        if (checkInDate == null || checkOutDate == null) {
            throw new ValidationException("Check-in and Check-out dates are required.");
        }
        if (!ValidationUtil.isValidDateRange(checkInDate, checkOutDate)) {
            throw new ValidationException("Check-out date must be strictly after check-in date.");
        }
        if (numberOfGuests <= 0) {
            numberOfGuests = 1;
        }
        return roomDAO.findAvailableRooms(checkInDate, checkOutDate, roomType, numberOfGuests, floor);
    }

    public int addRoom(Room room) {
        validateRoom(room);
        Room existing = roomDAO.findByRoomNumber(room.getRoomNumber().trim());
        if (existing != null) {
            throw new ValidationException("Room number '" + room.getRoomNumber() + "' already exists in inventory.");
        }
        return roomDAO.create(room);
    }

    public void updateRoom(Room room) {
        validateRoom(room);
        Room existing = roomDAO.findByRoomNumber(room.getRoomNumber().trim());
        if (existing != null && !existing.getRoomId().equals(room.getRoomId())) {
            throw new ValidationException("Another room with number '" + room.getRoomNumber() + "' already exists.");
        }
        roomDAO.update(room);
    }

    public void updateRoomStatus(int roomId, RoomStatus status) {
        if (status == null) throw new ValidationException("Status cannot be null.");
        roomDAO.updateStatus(roomId, status);
    }

    public void deleteRoom(int roomId) {
        roomDAO.delete(roomId);
    }

    private void validateRoom(Room room) {
        if (room == null) throw new ValidationException("Room details cannot be null.");
        if (!ValidationUtil.isNotEmpty(room.getRoomNumber())) throw new ValidationException("Room number is required.");
        if (room.getRoomType() == null) throw new ValidationException("Room type is required.");
        if (room.getFloor() == null || room.getFloor() <= 0) throw new ValidationException("Floor number must be greater than 0.");
        if (room.getPricePerNight() == null || !ValidationUtil.isNonNegative(room.getPricePerNight())) {
            throw new ValidationException("Room price per night must be non-negative.");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ValidationException("Room capacity must be at least 1 guest.");
        }
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.Available);
        }
    }
}
