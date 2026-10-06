package com.hotelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GuestService {
    private Integer guestServiceId;
    private Integer reservationId;
    private Integer serviceId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime serviceDate;
    private String notes;
    private LocalDateTime createdAt;

    // View Helper Fields
    private String serviceName;
    private String serviceCategory;
    private String guestName;
    private String roomNumber;

    public GuestService() {}

    public GuestService(Integer guestServiceId, Integer reservationId, Integer serviceId,
                        Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice, LocalDateTime serviceDate, String notes) {
        this.guestServiceId = guestServiceId;
        this.reservationId = reservationId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.serviceDate = serviceDate;
        this.notes = notes;
    }

    public Integer getGuestServiceId() { return guestServiceId; }
    public void setGuestServiceId(Integer guestServiceId) { this.guestServiceId = guestServiceId; }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDateTime serviceDate) { this.serviceDate = serviceDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
