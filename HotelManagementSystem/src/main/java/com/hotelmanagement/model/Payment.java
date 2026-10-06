package com.hotelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private Integer paymentId;
    private Integer invoiceId;
    private Integer reservationId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String paymentStatus; // Success, Pending, Failed, Refunded
    private String transactionReference;
    private LocalDateTime paymentDate;
    private String notes;
    private Integer processedByUserId;
    private LocalDateTime createdAt;

    // View Helper Fields
    private String invoiceNumber;
    private String guestName;
    private String roomNumber;
    private String processedByStaffName;

    public Payment() {}

    public Payment(Integer paymentId, Integer invoiceId, Integer reservationId, BigDecimal amount,
                   PaymentMethod paymentMethod, String paymentStatus, String transactionReference,
                   LocalDateTime paymentDate, String notes, Integer processedByUserId) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.paymentDate = paymentDate;
        this.notes = notes;
        this.processedByUserId = processedByUserId;
    }

    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getProcessedByUserId() { return processedByUserId; }
    public void setProcessedByUserId(Integer processedByUserId) { this.processedByUserId = processedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getProcessedByStaffName() { return processedByStaffName; }
    public void setProcessedByStaffName(String processedByStaffName) { this.processedByStaffName = processedByStaffName; }
}
