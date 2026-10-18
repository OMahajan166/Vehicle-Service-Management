package com.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vehicle number is required")
    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    @NotBlank(message = "Model is required")
    @Column(nullable = false)
    private String model;

    @NotBlank(message = "Company is required")
    @Column(nullable = false)
    private String company;

    @NotBlank(message = "Fuel type is required")
    @Column(nullable = false)
    private String fuelType;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Vehicle() {
    }

    public Vehicle(Long id, String vehicleNumber, String model,
                   String company, String fuelType,
                   Long customerId, LocalDateTime createdAt) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.model = model;
        this.company = company;
        this.fuelType = fuelType;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber =
                vehicleNumber != null
                        ? vehicleNumber.trim().toUpperCase()
                        : null;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model != null ? model.trim() : null;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company != null ? company.trim() : null;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}