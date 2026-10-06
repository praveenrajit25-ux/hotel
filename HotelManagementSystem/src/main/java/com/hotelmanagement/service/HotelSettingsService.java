package com.hotelmanagement.service;

import com.hotelmanagement.dao.HotelSettingsDAO;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.HotelSettings;
import com.hotelmanagement.util.ValidationUtil;

public class HotelSettingsService {

    private final HotelSettingsDAO hotelSettingsDAO;

    public HotelSettingsService() {
        this.hotelSettingsDAO = new HotelSettingsDAO();
    }

    public HotelSettings getSettings() {
        return hotelSettingsDAO.getSettings();
    }

    public void updateSettings(HotelSettings s) {
        if (s == null) throw new ValidationException("Settings cannot be null.");
        if (!ValidationUtil.isNotEmpty(s.getHotelName())) throw new ValidationException("Hotel name is required.");
        if (s.getTaxPercentage() == null || !ValidationUtil.isNonNegative(s.getTaxPercentage())) {
            throw new ValidationException("Tax percentage must be non-negative.");
        }
        hotelSettingsDAO.updateSettings(s);
    }
}
