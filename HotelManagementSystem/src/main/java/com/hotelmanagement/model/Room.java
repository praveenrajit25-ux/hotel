package com.hotelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Room {
    private Integer roomId;
    private String roomNumber;
    private RoomType roomType;
    private Integer floor;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private RoomStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Room() {}

    public Room(Integer roomId, String roomNumber, RoomType roomType, Integer floor,
                BigDecimal pricePerNight, Integer capacity, RoomStatus status, String description) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.floor = floor;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.status = status;
        this.description = description;
    }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + roomType + " - Fl " + floor + " - $" + pricePerNight + "/night)";
    }
}
