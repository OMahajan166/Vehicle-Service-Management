package com.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")

public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Double laborCost;

    @Column(nullable = false)
    private Double spareParts;

    @Column(nullable = false)
    private Double tax;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String status; // Pending, Paid

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        totalAmount = laborCost + spareParts + tax;
    }

	public Bill(Long id, Long serviceId, Long customerId, Double laborCost, Double spareParts, Double tax,
			Double totalAmount, String status, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.serviceId = serviceId;
		this.customerId = customerId;
		this.laborCost = laborCost;
		this.spareParts = spareParts;
		this.tax = tax;
		this.totalAmount = totalAmount;
		this.status = status;
		this.createdAt = createdAt;
	}
	

	public Bill() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Double getLaborCost() {
		return laborCost;
	}

	public void setLaborCost(Double laborCost) {
		this.laborCost = laborCost;
	}

	public Double getSpareParts() {
		return spareParts;
	}

	public void setSpareParts(Double spareParts) {
		this.spareParts = spareParts;
	}

	public Double getTax() {
		return tax;
	}

	public void setTax(Double tax) {
		this.tax = tax;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
