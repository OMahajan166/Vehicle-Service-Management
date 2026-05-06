package com.project.service;

import com.project.model.Bill;
import com.project.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;

    public Bill generateBill(Bill bill) {
        bill.setStatus("Pending");
        bill.setTotalAmount(bill.getLaborCost() + bill.getSpareParts() + bill.getTax());
        return billRepository.save(bill);
    }

    public List<Bill> getBillsByCustomerId(Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    public Optional<Bill> getBillById(Long id) {
        return billRepository.findById(id);
    }

    public Optional<Bill> getBillByServiceId(Long serviceId) {
        return billRepository.findByServiceId(serviceId);
    }

    public Bill updateBill(Bill bill) {
        return billRepository.save(bill);
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Double getTotalRevenue() {
        return getAllBills().stream()
                .mapToDouble(Bill::getTotalAmount)
                .sum();
    }
}
