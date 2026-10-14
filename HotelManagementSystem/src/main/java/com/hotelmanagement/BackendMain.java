package com.hotelmanagement;

import com.hotelmanagement.server.HotelBackendServer;

/**
 * =========================================================================
 * KR LUXE HOTEL - JAVA BACKEND APPLICATION ENTRY POINT
 * =========================================================================
 * Starts the Java REST backend server on port 8080 (or PORT env variable).
 *
 * Can be run via:
 *   mvn compile exec:java -Dexec.mainClass="com.hotelmanagement.BackendMain"
 *   or run_java_backend.bat
 * =========================================================================
 */
public class BackendMain {

    public static void main(String[] args) {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {}
        }

        try {
            HotelBackendServer server = new HotelBackendServer(port);
            server.start();

            // Keep alive
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            System.out.println("Java Backend ready and listening on http://localhost:" + port);
        } catch (Exception e) {
            System.err.println("Failed to start Java backend server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
