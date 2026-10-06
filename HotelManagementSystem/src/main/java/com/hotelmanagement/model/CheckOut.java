package com.hotelmanagement.model;

import java.time.LocalDateTime;

public class CheckOut {
    private Integer checkOutId;
    private Integer reservationId;
    private LocalDateTime checkOutTime;
    private boolean roomInspected;
    private boolean keyReturned;
    private String notes;
    private Integer processedByUserId;
    private LocalDateTime createdAt;

    // View Helper Fields
    private String guestName;
    private String roomNumber;
    private String processedByStaffName;

    public CheckOut() {}

    public CheckOut(Integer checkOutId, Integer reservationId, LocalDateTime checkOutTime,
                    boolean roomInspected, boolean keyReturned, String notes, Integer processedByUserId) {
        this.checkOutId = checkOutId;
        this.reservationId = reservationId;
        this.checkOutTime = checkOutTime;
        this.roomInspected = roomInspected;
        this.keyReturned = keyReturned;
        this.notes = notes;
        this.processedByUserId = processedByUserId;
    }

    public Integer getCheckOutId() { return checkOutId; }
    public void setCheckOutId(Integer checkOutId) { this.checkOutId = checkOutId; }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public boolean isRoomInspected() { return roomInspected; }
    public void setRoomInspected(boolean roomInspected) { this.roomInspected = roomInspected; }

    public boolean isKeyReturned() { return keyReturned; }
    public void setKeyReturned(boolean keyReturned) { this.keyReturned = keyReturned; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getProcessedByUserId() { return processedByUserId; }
    public void setProcessedByUserId(Integer processedByUserId) { this.processedByUserId = processedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getProcessedByStaffName() { return processedByStaffName; }
    public void setProcessedByStaffName(String processedByStaffName) { this.processedByStaffName = processedByStaffName; }
}
