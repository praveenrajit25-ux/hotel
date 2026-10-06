package com.hotelmanagement.service;

import com.hotelmanagement.dao.ReportDAO;
import com.hotelmanagement.model.DashboardStats;
import java.util.Map;

public class DashboardService {

    private final ReportDAO reportDAO;

    public DashboardService() {
        this.reportDAO = new ReportDAO();
    }

    public DashboardStats getStats() {
        return reportDAO.getDashboardStats();
    }

    public Map<String, Object> getOccupancySummary() {
        return reportDAO.getOccupancySummary();
    }
}
