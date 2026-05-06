package com.project.service;

import com.project.model.Service;
import com.project.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    public Service bookService(Service service) {
        service.setStatus("Pending");
        service.setCreatedAt(LocalDateTime.now());
        return serviceRepository.save(service);
    }

    public List<Service> getServicesByCustomerId(Long customerId) {
        return serviceRepository.findByCustomerId(customerId);
    }

    public List<Service> getServicesByMechanicId(Long mechanicId) {
        return serviceRepository.findByMechanicId(mechanicId);
    }

    public List<Service> getServicesByStatus(String status) {
        return serviceRepository.findByStatus(status);
    }

    public Optional<Service> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public Service updateService(Service service) {
        service.setUpdatedAt(LocalDateTime.now());
        return serviceRepository.save(service);
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public long getPendingServicesCount() {
        return getServicesByStatus("Pending").size();
    }

    public long getCompletedServicesCount() {
        return getServicesByStatus("Completed").size();
    }
}
