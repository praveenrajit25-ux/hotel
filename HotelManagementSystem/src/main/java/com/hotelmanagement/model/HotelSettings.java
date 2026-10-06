package com.hotelmanagement.model;

import java.math.BigDecimal;

public class HotelSettings {
    private Integer settingId;
    private String hotelName;
    private String tagline;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String currency;
    private BigDecimal taxPercentage;
    private String checkinTime;
    private String checkoutTime;
    private String invoiceFooter;

    public HotelSettings() {
        this.hotelName = "Grand Heritage Palace";
        this.currency = "$";
        this.taxPercentage = new BigDecimal("12.50");
        this.checkinTime = "14:00";
        this.checkoutTime = "11:00";
    }

    public Integer getSettingId() { return settingId; }
    public void setSettingId(Integer settingId) { this.settingId = settingId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public String getCheckinTime() { return checkinTime; }
    public void setCheckinTime(String checkinTime) { this.checkinTime = checkinTime; }

    public String getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(String checkoutTime) { this.checkoutTime = checkoutTime; }

    public String getInvoiceFooter() { return invoiceFooter; }
    public void setInvoiceFooter(String invoiceFooter) { this.invoiceFooter = invoiceFooter; }
}
