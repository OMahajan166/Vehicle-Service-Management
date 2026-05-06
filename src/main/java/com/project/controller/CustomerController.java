package com.project.controller;

import com.project.model.Bill;
import com.project.model.Service;
import com.project.model.Vehicle;
import com.project.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private BillService billService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("userId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }
        
        List<Vehicle> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        List<Service> services = serviceService.getServicesByCustomerId(customerId);
        List<Bill> bills = billService.getBillsByCustomerId(customerId);
        
        model.addAttribute("totalBookings", services.size());
        model.addAttribute("activeServices", services.stream().filter(s -> !"Completed".equals(s.getStatus())).count());
        model.addAttribute("completedServices", services.stream().filter(s -> "Completed".equals(s.getStatus())).count());
        model.addAttribute("totalBills", bills.size());
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("recentServices", services.stream().limit(5).toList());
        
        return "customer/dashboard";
    }

    @GetMapping("/vehicles")
    public String myVehicles(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("userId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }
        List<Vehicle> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        model.addAttribute("vehicles", vehicles);
        return "customer/my-vehicles";
    }

    @GetMapping("/add-vehicle")
    public String addVehiclePage(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "customer/add-vehicle";
    }

    @PostMapping("/add-vehicle")
    public String addVehicle(@ModelAttribute Vehicle vehicle, HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("userId");
        vehicle.setCustomerId(customerId);
        vehicleService.addVehicle(vehicle);
        return "redirect:/customer/vehicles";
    }

    @GetMapping("/book-service")
    public String bookServicePage(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("userId");
        List<Vehicle> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("service", new Service());
        return "customer/book-service";
    }

    @PostMapping("/book-service")
    public String bookService(@ModelAttribute Service service, HttpSession session) {
        Long customerId = (Long) session.getAttribute("userId");
        service.setCustomerId(customerId);
        serviceService.bookService(service);
        return "redirect:/customer/services";
    }

    @GetMapping("/services")
    public String myServices(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("userId");
        List<Service> services = serviceService.getServicesByCustomerId(customerId);
        model.addAttribute("services", services);
        return "customer/my-services";
    }

    @GetMapping("/track-service/{id}")
    public String trackService(@PathVariable Long id, Model model) {
        Optional<Service> service = serviceService.getServiceById(id);
        model.addAttribute("service", service.orElse(null));
        return "customer/track-service";
    }

    @GetMapping("/bills")
    public String myBills(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("userId");
        List<Bill> bills = billService.getBillsByCustomerId(customerId);
        model.addAttribute("bills", bills);
        return "customer/my-bills";
    }

    @GetMapping("/bill/{id}")
    public String viewBill(@PathVariable Long id, Model model) {
        Optional<Bill> bill = billService.getBillById(id);
        model.addAttribute("bill", bill.orElse(null));
        return "customer/view-bill";
    }
}
