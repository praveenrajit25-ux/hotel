package com.hotelmanagement.util;

import com.hotelmanagement.model.GuestService;
import com.hotelmanagement.model.HotelSettings;
import com.hotelmanagement.model.Invoice;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Utility for exporting invoices and reports to formatted text and printable documents.
 */
public class PDFExporter {

    public static File exportInvoiceToTextFile(Invoice invoice, HotelSettings settings, List<GuestService> services, File targetFile) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(targetFile))) {
            out.println("================================================================================");
            out.println("                         " + settings.getHotelName().toUpperCase());
            out.println("                         " + (settings.getTagline() != null ? settings.getTagline() : ""));
            out.println("================================================================================");
            out.println("Address: " + settings.getAddress());
            out.println("Phone:   " + settings.getPhone() + "  |  Email: " + settings.getEmail());
            out.println("--------------------------------------------------------------------------------");
            out.println("INVOICE NUMBER: " + invoice.getInvoiceNumber() + "           INVOICE DATE: " + DateUtil.formatDate(invoice.getInvoiceDate()));
            out.println("RESERVATION ID: #" + invoice.getReservationId() + "             PAYMENT STATUS: " + invoice.getPaymentStatus());
            out.println("--------------------------------------------------------------------------------");
            out.println("GUEST DETAILS:");
            out.println("Name:    " + invoice.getGuestName());
            out.println("Phone:   " + (invoice.getGuestPhone() != null ? invoice.getGuestPhone() : "-"));
            out.println("Email:   " + (invoice.getGuestEmail() != null ? invoice.getGuestEmail() : "-"));
            out.println("--------------------------------------------------------------------------------");
            out.println("STAY DETAILS:");
            out.println("Room Number: " + invoice.getRoomNumber() + " (" + invoice.getRoomType() + ")");
            out.println("Check-in:    " + DateUtil.formatDate(invoice.getCheckInDate()) + "  Check-out: " + DateUtil.formatDate(invoice.getCheckOutDate()));
            out.println("Total Stay:  " + invoice.getNumberOfNights() + " Night(s)");
            out.println("--------------------------------------------------------------------------------");
            out.println("ITEMIZED CHARGES:");
            out.printf("%-40s %10s %10s %15s%n", "Description", "Qty/Nights", "Unit Price", "Total (" + settings.getCurrency() + ")");
            out.println("--------------------------------------------------------------------------------");
            out.printf("%-40s %10d %10s %15s%n", "Room Stay Charges (" + invoice.getRoomNumber() + ")",
                    invoice.getNumberOfNights(),
                    CurrencyUtil.format(invoice.getRoomCharges().divide(java.math.BigDecimal.valueOf(Math.max(1, invoice.getNumberOfNights())), 2, java.math.RoundingMode.HALF_UP)),
                    CurrencyUtil.format(invoice.getRoomCharges()));

            if (services != null && !services.isEmpty()) {
                for (GuestService gs : services) {
                    out.printf("%-40s %10d %10s %15s%n",
                            gs.getServiceName(),
                            gs.getQuantity(),
                            CurrencyUtil.format(gs.getUnitPrice()),
                            CurrencyUtil.format(gs.getTotalPrice()));
                }
            }

            if (invoice.getAdditionalCharges() != null && invoice.getAdditionalCharges().compareTo(java.math.BigDecimal.ZERO) > 0) {
                out.printf("%-40s %10s %10s %15s%n", "Additional Miscellaneous Charges", "-", "-", CurrencyUtil.format(invoice.getAdditionalCharges()));
            }

            out.println("--------------------------------------------------------------------------------");
            out.printf("%-62s %15s%n", "Subtotal:", CurrencyUtil.format(invoice.getSubtotal()));
            if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                out.printf("%-62s %15s%n", "Discount:", "-" + CurrencyUtil.format(invoice.getDiscount()));
            }
            out.printf("%-62s %15s%n", "Tax (" + invoice.getTaxPercentage() + "%):", CurrencyUtil.format(invoice.getTaxAmount()));
            out.println("================================================================================");
            out.printf("%-62s %15s%n", "GRAND TOTAL:", CurrencyUtil.format(invoice.getGrandTotal()));
            out.printf("%-62s %15s%n", "Amount Paid (" + invoice.getPaymentMethod() + "):", CurrencyUtil.format(invoice.getPaidAmount()));
            out.printf("%-62s %15s%n", "BALANCE DUE:", CurrencyUtil.format(invoice.getBalanceAmount()));
            out.println("================================================================================");
            out.println();
            out.println("NOTE / FOOTER:");
            out.println(settings.getInvoiceFooter() != null ? settings.getInvoiceFooter() : "Thank you for staying with us!");
            out.println("================================================================================");
        }
        return targetFile;
    }
}
