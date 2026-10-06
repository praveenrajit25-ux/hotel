package com.hotelmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Service {
    private Integer serviceId;
    private String serviceName;
    private String category; // Dining, Wellness, Housekeeping, Transport, Amenities, Other
    private String description;
    private BigDecimal price;
    private String status; // Active, Inactive
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Service() {}

    public Service(Integer serviceId, String serviceName, String category, String description, BigDecimal price, String status) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.category = category;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return serviceName + " ($" + price + " - " + category + ")";
    }
}
