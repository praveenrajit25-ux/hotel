package com.hotelmanagement.model;

import java.math.BigDecimal;

public class DashboardStats {
    private int totalRooms;
    private int availableRooms;
    private int reservedRooms;
    private int occupiedRooms;
    private int cleaningRooms;
    private int maintenanceRooms;
    private int totalGuests;
    private int todayCheckIns;
    private int todayCheckOuts;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private int activeReservations;

    public DashboardStats() {
        this.totalRevenue = BigDecimal.ZERO;
        this.todayRevenue = BigDecimal.ZERO;
    }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public int getReservedRooms() { return reservedRooms; }
    public void setReservedRooms(int reservedRooms) { this.reservedRooms = reservedRooms; }

    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }

    public int getCleaningRooms() { return cleaningRooms; }
    public void setCleaningRooms(int cleaningRooms) { this.cleaningRooms = cleaningRooms; }

    public int getMaintenanceRooms() { return maintenanceRooms; }
    public void setMaintenanceRooms(int maintenanceRooms) { this.maintenanceRooms = maintenanceRooms; }

    public int getTotalGuests() { return totalGuests; }
    public void setTotalGuests(int totalGuests) { this.totalGuests = totalGuests; }

    public int getTodayCheckIns() { return todayCheckIns; }
    public void setTodayCheckIns(int todayCheckIns) { this.todayCheckIns = todayCheckIns; }

    public int getTodayCheckOuts() { return todayCheckOuts; }
    public void setTodayCheckOuts(int todayCheckOuts) { this.todayCheckOuts = todayCheckOuts; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public int getActiveReservations() { return activeReservations; }
    public void setActiveReservations(int activeReservations) { this.activeReservations = activeReservations; }
}
