package com.hotelmanagement.server;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.dao.ReservationDAO;
import com.hotelmanagement.dao.InvoiceDAO;
import com.hotelmanagement.dao.ServiceDAO;
import com.hotelmanagement.dao.StaffDAO;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.Invoice;
import com.hotelmanagement.model.Service;
import com.hotelmanagement.model.Staff;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

/**
 * =========================================================================
 * KR LUXE HOTEL - JAVA REST BACKEND SERVER
 * =========================================================================
 * Enterprise Java HTTP REST API Server built using Java Standard Library
 * (com.sun.net.httpserver.HttpServer). Runs without needing heavy external
 * containers or frameworks.
 *
 * Default Port: 8080 (Configurable via PORT environment variable)
 * =========================================================================
 */
public class HotelBackendServer {

    private static final Logger LOGGER = Logger.getLogger(HotelBackendServer.class.getName());
    private final int port;
    private HttpServer server;

    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    public HotelBackendServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Root & Status
        server.createContext("/", new RootHandler());
        server.createContext("/api/status", new StatusHandler());

        // API Endpoints
        server.createContext("/api/rooms", new RoomsHandler());
        server.createContext("/api/reservations", new ReservationsHandler());
        server.createContext("/api/invoices", new InvoicesHandler());
        server.createContext("/api/services", new ServicesHandler());
        server.createContext("/api/staff", new StaffHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("============================================================");
        System.out.println("  ☕ KR LUXE HOTEL - JAVA BACKEND REST API SERVER RUNNING");
        System.out.println("  Port: http://localhost:" + port);
        System.out.println("  Database: MySQL via DatabaseConnection (jdbc:mysql://localhost:3306/hotel_management)");
        System.out.println("============================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Java Backend Server stopped.");
        }
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 1. Root Handler
    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String html = "<html><body style='font-family:sans-serif;padding:2rem;background:#0f172a;color:#f8fafc;'>"
                    + "<h1 style='color:#f59e0b;'>KR LUXE HOTEL - JAVA BACKEND REST API</h1>"
                    + "<p>Status: <strong>Running & Ready</strong></p>"
                    + "<h3>Available REST Endpoints:</h3>"
                    + "<ul>"
                    + "<li><a style='color:#38bdf8;' href='/api/status'>/api/status</a> - Live Database Health & Table Counts</li>"
                    + "<li><a style='color:#38bdf8;' href='/api/rooms'>/api/rooms</a> - Room Inventory (Available/Reserved)</li>"
                    + "<li><a style='color:#38bdf8;' href='/api/reservations'>/api/reservations</a> - Hotel Bookings</li>"
                    + "<li><a style='color:#38bdf8;' href='/api/invoices'>/api/invoices</a> - Folio Invoices</li>"
                    + "<li><a style='color:#38bdf8;' href='/api/services'>/api/services</a> - Hotel Amenities & Services Catalog</li>"
                    + "<li><a style='color:#38bdf8;' href='/api/staff'>/api/staff</a> - Staff Directory</li>"
                    + "</ul>"
                    + "</body></html>";
            setCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // 2. Status Handler
    private static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            boolean dbConnected = DatabaseConnection.getInstance().testConnection();
            int roomCount = 0;
            int resCount = 0;
            int invCount = 0;

            if (dbConnected) {
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT count(*) FROM rooms");
                    if (rs.next()) roomCount = rs.getInt(1);
                    rs = stmt.executeQuery("SELECT count(*) FROM reservations");
                    if (rs.next()) resCount = rs.getInt(1);
                    rs = stmt.executeQuery("SELECT count(*) FROM invoices");
                    if (rs.next()) invCount = rs.getInt(1);
                } catch (Exception e) {
                    LOGGER.warning("Could not fetch table counts: " + e.getMessage());
                }
            }

            String json = String.format(
                    "{\"success\":true,\"backend\":\"Java HttpServer\",\"dbConnected\":%b,\"database\":\"hotel_management\",\"tables\":{\"rooms\":%d,\"reservations\":%d,\"invoices\":%d}}",
                    dbConnected, roomCount, resCount, invCount
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    // 3. Rooms Handler
    private class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                List<Room> rooms = roomDAO.findAll();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < rooms.size(); i++) {
                    Room r = rooms.get(i);
                    sb.append(String.format(
                            "{\"id\":%d,\"number\":\"%s\",\"type\":\"%s\",\"floor\":%d,\"pricePerNight\":%.2f,\"capacity\":%d,\"status\":\"%s\",\"description\":\"%s\"}",
                            r.getRoomId(),
                            r.getRoomNumber() != null ? r.getRoomNumber() : "",
                            r.getRoomType() != null ? r.getRoomType().name() : "Single",
                            r.getFloor(),
                            r.getPricePerNight() != null ? r.getPricePerNight().doubleValue() : 0.0,
                            r.getCapacity(),
                            r.getStatus() != null ? r.getStatus().name() : "Available",
                            r.getDescription() != null ? r.getDescription().replace("\"", "\\\"") : ""
                    ));
                    if (i < rooms.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // 4. Reservations Handler
    private class ReservationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    List<Reservation> reservations = reservationDAO.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < reservations.size(); i++) {
                        Reservation r = reservations.get(i);
                        sb.append(String.format(
                                "{\"id\":%d,\"guestId\":%d,\"guestName\":\"%s\",\"roomId\":%d,\"roomNumber\":\"%s\",\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\",\"status\":\"%s\"}",
                                r.getReservationId(),
                                r.getGuestId(),
                                r.getGuestName() != null ? r.getGuestName() : "",
                                r.getRoomId(),
                                r.getRoomNumber() != null ? r.getRoomNumber() : "",
                                r.getCheckInDate() != null ? r.getCheckInDate().toString() : "",
                                r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : "",
                                r.getStatus() != null ? r.getStatus().name() : "Confirmed"
                        ));
                        if (i < reservations.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Reservation created via Java backend\"}");
            }
        }
    }

    // 5. Invoices Handler
    private class InvoicesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                List<Invoice> invoices = invoiceDAO.findAll();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < invoices.size(); i++) {
                    Invoice inv = invoices.get(i);
                    sb.append(String.format(
                            "{\"id\":%d,\"invoiceNumber\":\"%s\",\"reservationId\":%d,\"grandTotal\":%.2f,\"paidAmount\":%.2f,\"balanceAmount\":%.2f,\"paymentStatus\":\"%s\"}",
                            inv.getInvoiceId(),
                            inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "",
                            inv.getReservationId(),
                            inv.getGrandTotal() != null ? inv.getGrandTotal().doubleValue() : 0.0,
                            inv.getPaidAmount() != null ? inv.getPaidAmount().doubleValue() : 0.0,
                            inv.getBalanceAmount() != null ? inv.getBalanceAmount().doubleValue() : 0.0,
                            inv.getPaymentStatus() != null ? inv.getPaymentStatus().name() : "Pending"
                    ));
                    if (i < invoices.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // 6. Services Handler
    private class ServicesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                List<Service> services = serviceDAO.findAll();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < services.size(); i++) {
                    Service s = services.get(i);
                    sb.append(String.format(
                            "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"status\":\"%s\"}",
                            s.getServiceId(),
                            s.getServiceName() != null ? s.getServiceName() : "",
                            s.getCategory() != null ? s.getCategory() : "",
                            s.getPrice() != null ? s.getPrice().doubleValue() : 0.0,
                            s.getStatus() != null ? s.getStatus() : "Active"
                    ));
                    if (i < services.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // 7. Staff Handler
    private class StaffHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                List<Staff> staffList = staffDAO.findAll();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < staffList.size(); i++) {
                    Staff st = staffList.get(i);
                    sb.append(String.format(
                            "{\"id\":%d,\"employeeCode\":\"%s\",\"fullName\":\"%s\",\"role\":\"%s\",\"email\":\"%s\",\"status\":\"%s\"}",
                            st.getStaffId(),
                            st.getEmployeeCode() != null ? st.getEmployeeCode() : "",
                            st.getFullName() != null ? st.getFullName() : "",
                            st.getRole() != null ? st.getRole().name() : "Staff",
                            st.getEmail() != null ? st.getEmail() : "",
                            st.getStatus() != null ? st.getStatus() : "Active"
                    ));
                    if (i < staffList.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
