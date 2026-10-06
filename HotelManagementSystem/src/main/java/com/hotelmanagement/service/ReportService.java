package com.hotelmanagement.service;

import com.hotelmanagement.dao.*;
import com.hotelmanagement.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final ReportDAO reportDAO;
    private final ReservationDAO reservationDAO;
    private final GuestDAO guestDAO;
    private final RoomDAO roomDAO;
    private final PaymentDAO paymentDAO;
    private final CheckInDAO checkInDAO;
    private final CheckOutDAO checkOutDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
        this.reservationDAO = new ReservationDAO();
        this.guestDAO = new GuestDAO();
        this.roomDAO = new RoomDAO();
        this.paymentDAO = new PaymentDAO();
        this.checkInDAO = new CheckInDAO();
        this.checkOutDAO = new CheckOutDAO();
    }

    public List<Map<String, Object>> getDailyRevenue(LocalDate fromDate, LocalDate toDate) {
        return reportDAO.getDailyRevenueReport(fromDate, toDate);
    }

    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        return reportDAO.getMonthlyRevenueReport(year);
    }

    public List<Map<String, Object>> getServiceRevenue(LocalDate fromDate, LocalDate toDate) {
        return reportDAO.getServiceRevenueReport(fromDate, toDate);
    }

    public List<Reservation> getReservationReport(LocalDate fromDate, LocalDate toDate, String status) {
        return reservationDAO.search("", status, fromDate, toDate);
    }

    public List<Guest> getGuestReport() {
        return guestDAO.findAll();
    }

    public List<Room> getRoomStatusReport() {
        return roomDAO.findAll();
    }

    public List<Payment> getPaymentReport(LocalDate fromDate, LocalDate toDate, String method) {
        return paymentDAO.search("", method, fromDate, toDate);
    }

    public List<CheckIn> getCheckInReport() {
        return checkInDAO.findAll();
    }

    public List<CheckOut> getCheckOutReport() {
        return checkOutDAO.findAll();
    }
}
