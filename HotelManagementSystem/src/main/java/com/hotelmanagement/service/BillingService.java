package com.hotelmanagement.service;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.dao.GuestServiceDAO;
import com.hotelmanagement.dao.HotelSettingsDAO;
import com.hotelmanagement.dao.InvoiceDAO;
import com.hotelmanagement.dao.ReservationDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.*;
import com.hotelmanagement.util.CurrencyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class BillingService {

    private final InvoiceDAO invoiceDAO;
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final GuestServiceDAO guestServiceDAO;
    private final HotelSettingsDAO hotelSettingsDAO;

    public BillingService() {
        this.invoiceDAO = new InvoiceDAO();
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.guestServiceDAO = new GuestServiceDAO();
        this.hotelSettingsDAO = new HotelSettingsDAO();
    }

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }

    public Invoice getInvoiceById(int invoiceId) {
        return invoiceDAO.findById(invoiceId);
    }

    public Invoice getInvoiceByReservationId(int reservationId) {
        return invoiceDAO.findByReservationId(reservationId);
    }

    public List<Invoice> searchInvoices(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        return invoiceDAO.search(keyword, status, fromDate, toDate);
    }

    /**
     * Prepares/Calculates a fresh Invoice model for a given reservation without committing it to DB.
     */
    public Invoice calculateInvoicePreview(int reservationId, BigDecimal customDiscount, BigDecimal customAdditionalCharges, PaymentMethod method) {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) throw new ValidationException("Reservation #" + reservationId + " not found.");

        Room room = roomDAO.findById(res.getRoomId());
        HotelSettings settings = hotelSettingsDAO.getSettings();

        long nights = ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate());
        if (nights <= 0) nights = 1;

        BigDecimal pricePerNight = (room != null && room.getPricePerNight() != null) ? room.getPricePerNight() : BigDecimal.ZERO;
        BigDecimal roomCharges = pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        // Calculate service charges
        List<GuestService> services = guestServiceDAO.findByReservationId(reservationId);
        BigDecimal serviceCharges = BigDecimal.ZERO;
        for (GuestService gs : services) {
            if (gs.getTotalPrice() != null) {
                serviceCharges = serviceCharges.add(gs.getTotalPrice());
            }
        }
        serviceCharges = serviceCharges.setScale(2, RoundingMode.HALF_UP);

        BigDecimal additionalCharges = customAdditionalCharges != null ? customAdditionalCharges.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal subtotal = roomCharges.add(serviceCharges).add(additionalCharges).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = customDiscount != null ? customDiscount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) discount = subtotal;

        BigDecimal taxPercentage = settings.getTaxPercentage() != null ? settings.getTaxPercentage() : new BigDecimal("10.00");
        BigDecimal taxableBase = subtotal.subtract(discount);
        BigDecimal taxAmount = taxableBase.multiply(taxPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = taxableBase.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        Invoice inv = new Invoice();
        inv.setReservationId(reservationId);
        inv.setGuestId(res.getGuestId());
        inv.setRoomId(res.getRoomId());
        inv.setCheckInDate(res.getCheckInDate());
        inv.setCheckOutDate(res.getCheckOutDate());
        inv.setNumberOfNights((int) nights);
        inv.setRoomCharges(roomCharges);
        inv.setServiceCharges(serviceCharges);
        inv.setAdditionalCharges(additionalCharges);
        inv.setSubtotal(subtotal);
        inv.setDiscount(discount);
        inv.setTaxPercentage(taxPercentage);
        inv.setTaxAmount(taxAmount);
        inv.setGrandTotal(grandTotal);
        inv.setPaidAmount(BigDecimal.ZERO);
        inv.setBalanceAmount(grandTotal);
        inv.setPaymentStatus(PaymentStatus.Pending);
        inv.setPaymentMethod(method != null ? method : PaymentMethod.Cash);
        inv.setInvoiceDate(LocalDate.now());

        inv.setGuestName(res.getGuestName());
        inv.setGuestPhone(res.getGuestPhone());
        inv.setGuestEmail(res.getGuestEmail());
        inv.setRoomNumber(res.getRoomNumber());
        inv.setRoomType(res.getRoomType());

        return inv;
    }

    /**
     * Generates or updates an Invoice in the database.
     */
    public Invoice generateOrUpdateInvoice(int reservationId, BigDecimal customDiscount, BigDecimal customAdditionalCharges, PaymentMethod method) {
        Invoice preview = calculateInvoicePreview(reservationId, customDiscount, customAdditionalCharges, method);
        Invoice existing = invoiceDAO.findByReservationId(reservationId);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existing == null) {
                    // Generate unique Invoice Number (e.g. INV-2026-XXXX)
                    String invNum = "INV-" + LocalDate.now().getYear() + "-" + (1000 + (int)(Math.random() * 9000));
                    preview.setInvoiceNumber(invNum);
                    int newId = invoiceDAO.create(conn, preview);
                    preview.setInvoiceId(newId);
                } else {
                    preview.setInvoiceId(existing.getInvoiceId());
                    preview.setInvoiceNumber(existing.getInvoiceNumber());
                    preview.setPaidAmount(existing.getPaidAmount());
                    BigDecimal balance = preview.getGrandTotal().subtract(existing.getPaidAmount()).setScale(2, RoundingMode.HALF_UP);
                    preview.setBalanceAmount(balance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : balance);

                    if (preview.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        preview.setPaymentStatus(PaymentStatus.Paid);
                    } else if (preview.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                        preview.setPaymentStatus(PaymentStatus.Partially_Paid);
                    } else {
                        preview.setPaymentStatus(PaymentStatus.Pending);
                    }

                    invoiceDAO.update(conn, preview);
                }
                conn.commit();
                return preview;
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Failed to generate invoice: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error generating invoice", e);
        }
    }
}
