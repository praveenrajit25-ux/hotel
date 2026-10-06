package com.hotelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Invoice {
    private Integer invoiceId;
    private String invoiceNumber;
    private Integer reservationId;
    private Integer guestId;
    private Integer roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;
    private BigDecimal roomCharges;
    private BigDecimal serviceCharges;
    private BigDecimal additionalCharges;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDate invoiceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // View Helper Fields
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String roomNumber;
    private RoomType roomType;

    public Invoice() {
        this.roomCharges = BigDecimal.ZERO;
        this.serviceCharges = BigDecimal.ZERO;
        this.additionalCharges = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.taxPercentage = new BigDecimal("12.50");
        this.taxAmount = BigDecimal.ZERO;
        this.grandTotal = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.balanceAmount = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.Unpaid;
        this.paymentMethod = PaymentMethod.Card;
    }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public Integer getGuestId() { return guestId; }
    public void setGuestId(Integer guestId) { this.guestId = guestId; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(Integer numberOfNights) { this.numberOfNights = numberOfNights; }

    public BigDecimal getRoomCharges() { return roomCharges; }
    public void setRoomCharges(BigDecimal roomCharges) { this.roomCharges = roomCharges; }

    public BigDecimal getServiceCharges() { return serviceCharges; }
    public void setServiceCharges(BigDecimal serviceCharges) { this.serviceCharges = serviceCharges; }

    public BigDecimal getAdditionalCharges() { return additionalCharges; }
    public void setAdditionalCharges(BigDecimal additionalCharges) { this.additionalCharges = additionalCharges; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(BigDecimal balanceAmount) { this.balanceAmount = balanceAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
}
