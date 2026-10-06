package com.hotelmanagement.service;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.dao.InvoiceDAO;
import com.hotelmanagement.dao.PaymentDAO;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.exception.PaymentException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.Invoice;
import com.hotelmanagement.model.Payment;
import com.hotelmanagement.model.PaymentMethod;
import com.hotelmanagement.model.PaymentStatus;
import com.hotelmanagement.util.ValidationUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final InvoiceDAO invoiceDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }

    public List<Payment> getPaymentsForInvoice(int invoiceId) {
        return paymentDAO.findByInvoiceId(invoiceId);
    }

    public List<Payment> searchPayments(String keyword, String method, LocalDate fromDate, LocalDate toDate) {
        return paymentDAO.search(keyword, method, fromDate, toDate);
    }

    /**
     * Processes a payment transaction atomically.
     */
    public int processPayment(int invoiceId, BigDecimal amount, PaymentMethod method,
                              String transactionRef, String notes, Integer staffUserId) {
        if (!ValidationUtil.isPositive(amount)) {
            throw new ValidationException("Payment amount must be greater than zero.");
        }
        if (method == null) {
            throw new ValidationException("Payment method is required.");
        }

        Invoice invoice = invoiceDAO.findById(invoiceId);
        if (invoice == null) {
            throw new PaymentException("Invoice #" + invoiceId + " not found.");
        }

        if (invoice.getBalanceAmount() != null && invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("This invoice is already fully paid.");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Payment payment = new Payment();
                payment.setInvoiceId(invoiceId);
                payment.setReservationId(invoice.getReservationId());
                payment.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
                payment.setPaymentMethod(method);
                payment.setPaymentStatus("Success");
                payment.setTransactionReference(transactionRef != null && !transactionRef.trim().isEmpty() ?
                        transactionRef.trim() : "TXN-" + System.currentTimeMillis());
                payment.setPaymentDate(LocalDateTime.now());
                payment.setNotes(notes);
                payment.setProcessedByUserId(staffUserId);

                int paymentId = paymentDAO.create(conn, payment);

                // Recalculate Invoice Paid and Balance
                BigDecimal newPaid = (invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(amount).setScale(2, RoundingMode.HALF_UP);
                BigDecimal newBalance = invoice.getGrandTotal().subtract(newPaid).setScale(2, RoundingMode.HALF_UP);
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    newBalance = BigDecimal.ZERO;
                }

                PaymentStatus newStatus;
                if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    newStatus = PaymentStatus.Paid;
                } else {
                    newStatus = PaymentStatus.Partially_Paid;
                }

                invoiceDAO.updatePaymentStatusAndAmounts(conn, invoiceId, newPaid, newBalance, newStatus);

                conn.commit();
                return paymentId;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof PaymentException) throw (PaymentException) e;
                throw new DatabaseException("Failed to process payment: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during payment processing", e);
        }
    }
}
