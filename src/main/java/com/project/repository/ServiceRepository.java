package com.project.repository;

import com.project.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByCustomerId(Long customerId);
    List<Service> findByMechanicId(Long mechanicId);
    List<Service> findByStatus(String status);
}
