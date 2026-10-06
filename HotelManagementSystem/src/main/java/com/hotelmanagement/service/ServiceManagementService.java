package com.hotelmanagement.service;

import com.hotelmanagement.dao.GuestServiceDAO;
import com.hotelmanagement.dao.ServiceDAO;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.GuestService;
import com.hotelmanagement.model.Service;
import com.hotelmanagement.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ServiceManagementService {

    private final ServiceDAO serviceDAO;
    private final GuestServiceDAO guestServiceDAO;

    public ServiceManagementService() {
        this.serviceDAO = new ServiceDAO();
        this.guestServiceDAO = new GuestServiceDAO();
    }

    public List<Service> getAllServices() {
        return serviceDAO.findAll();
    }

    public List<Service> getActiveServices() {
        return serviceDAO.findActiveServices();
    }

    public Service getServiceById(int serviceId) {
        return serviceDAO.findById(serviceId);
    }

    public int addService(Service s) {
        validateService(s);
        return serviceDAO.create(s);
    }

    public void updateService(Service s) {
        validateService(s);
        serviceDAO.update(s);
    }

    public void deleteService(int serviceId) {
        serviceDAO.delete(serviceId);
    }

    public List<GuestService> getServicesForReservation(int reservationId) {
        return guestServiceDAO.findByReservationId(reservationId);
    }

    public List<GuestService> getAllGuestServices() {
        return guestServiceDAO.findAll();
    }

    public int chargeServiceToReservation(int reservationId, int serviceId, int quantity, String notes) {
        if (reservationId <= 0) throw new ValidationException("Invalid reservation ID.");
        if (serviceId <= 0) throw new ValidationException("Invalid service ID.");
        if (quantity <= 0) throw new ValidationException("Quantity must be at least 1.");

        Service service = serviceDAO.findById(serviceId);
        if (service == null) throw new ValidationException("Service not found.");

        BigDecimal total = service.getPrice().multiply(BigDecimal.valueOf(quantity));

        GuestService gs = new GuestService();
        gs.setReservationId(reservationId);
        gs.setServiceId(serviceId);
        gs.setQuantity(quantity);
        gs.setUnitPrice(service.getPrice());
        gs.setTotalPrice(total);
        gs.setServiceDate(LocalDateTime.now());
        gs.setNotes(notes);

        return guestServiceDAO.create(gs);
    }

    public void removeGuestService(int guestServiceId) {
        guestServiceDAO.delete(guestServiceId);
    }

    private void validateService(Service s) {
        if (s == null) throw new ValidationException("Service details cannot be null.");
        if (!ValidationUtil.isNotEmpty(s.getServiceName())) throw new ValidationException("Service name is required.");
        if (!ValidationUtil.isNotEmpty(s.getCategory())) throw new ValidationException("Service category is required.");
        if (s.getPrice() == null || !ValidationUtil.isNonNegative(s.getPrice())) {
            throw new ValidationException("Service price must be non-negative.");
        }
    }
}
