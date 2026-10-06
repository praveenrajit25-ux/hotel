package com.hotelmanagement.model;

import java.time.LocalDateTime;

public class CheckIn {
    private Integer checkInId;
    private Integer reservationId;
    private LocalDateTime checkInTime;
    private String roomKeyIssued;
    private boolean verifiedIdProof;
    private String notes;
    private Integer processedByUserId;
    private LocalDateTime createdAt;

    // View Helper Fields
    private String guestName;
    private String roomNumber;
    private String processedByStaffName;

    public CheckIn() {}

    public CheckIn(Integer checkInId, Integer reservationId, LocalDateTime checkInTime,
                   String roomKeyIssued, boolean verifiedIdProof, String notes, Integer processedByUserId) {
        this.checkInId = checkInId;
        this.reservationId = reservationId;
        this.checkInTime = checkInTime;
        this.roomKeyIssued = roomKeyIssued;
        this.verifiedIdProof = verifiedIdProof;
        this.notes = notes;
        this.processedByUserId = processedByUserId;
    }

    public Integer getCheckInId() { return checkInId; }
    public void setCheckInId(Integer checkInId) { this.checkInId = checkInId; }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public String getRoomKeyIssued() { return roomKeyIssued; }
    public void setRoomKeyIssued(String roomKeyIssued) { this.roomKeyIssued = roomKeyIssued; }

    public boolean isVerifiedIdProof() { return verifiedIdProof; }
    public void setVerifiedIdProof(boolean verifiedIdProof) { this.verifiedIdProof = verifiedIdProof; }

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
