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
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import com.project.config.CustomUserDetails;


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
    public String dashboard(Authentication authentication, Model model) {
    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();
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
    public String myVehicles(Authentication authentication, Model model) {
    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();
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

//    @PostMapping("/add-vehicle")
//    public String addVehicle(@ModelAttribute Vehicle vehicle, HttpSession session, Model model) {
//        Long customerId = (Long) session.getAttribute("userId");
//        vehicle.setCustomerId(customerId);
//        vehicleService.addVehicle(vehicle);
//        return "redirect:/customer/vehicles";
//    }
    
   // @PostMapping("/add-vehicle")
//    public String addVehicle(@ModelAttribute Vehicle vehicle,
//                             HttpSession session,
//                             Model model) {
//
//        try {
//            Long customerId = (Long) session.getAttribute("userId");
//            vehicle.setCustomerId(customerId);
//
//            vehicleService.addVehicle(vehicle);
//
//            return "redirect:/customer/vehicles";
//
//        } catch (RuntimeException e) {
//            model.addAttribute("vehicle", vehicle);
//            model.addAttribute("error", e.getMessage());
//
//            return "customer/add-vehicle";
//        }
//    }
    
    @PostMapping("/add-vehicle")
    public String addVehicle(
            @Valid
            @ModelAttribute("vehicle") Vehicle vehicle,
            BindingResult result,
            Authentication authentication,
            Model model) {

        if (result.hasErrors()) {
            return "customer/add-vehicle";
        }

        try {

        	CustomUserDetails user =
        	        (CustomUserDetails)
        	                authentication.getPrincipal();

        	Long customerId =
        	        user.getId();

            vehicle.setCustomerId(customerId);

            vehicleService.addVehicle(vehicle);

            return "redirect:/customer/vehicles";

        } catch (RuntimeException e) {

            model.addAttribute(
                    "error",
                    e.getMessage());

            return "customer/add-vehicle";
        }
    }

    @GetMapping("/book-service")
    public String bookServicePage(Authentication authentication, Model model) {
    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();
        List<Vehicle> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("service", new Service());
        return "customer/book-service";
    }

    @PostMapping("/book-service")
    public String bookService(
            @Valid
            @ModelAttribute("service") Service service,
            BindingResult result,
            Authentication authentication,
            Model model) {

    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();

        if (result.hasErrors()) {
            model.addAttribute(
                    "vehicles",
                    vehicleService.getVehiclesByCustomerId(customerId));

            return "customer/book-service";
        }

        service.setCustomerId(customerId);

        serviceService.bookService(service);

        return "redirect:/customer/services";
    }

    @GetMapping("/services")
    public String myServices(Authentication authentication, Model model) {
    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();
        List<Service> services = serviceService.getServicesByCustomerId(customerId);
        model.addAttribute("services", services);
        return "customer/my-services";
    }

    @GetMapping("/track-service/{id}")
    public String trackService(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        CustomUserDetails user =
        		(CustomUserDetails)
                	authentication.getPrincipal();

        Long customerId =
        		user.getId();

        // User not logged in
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        Optional<Service> serviceOptional =
                serviceService.getServiceById(id);

        // Service does not exist
        if (serviceOptional.isEmpty()) {
            return "redirect:/customer/services";
        }

        Service service =
                serviceOptional.get();

        // Service belongs to another customer
        if (!service.getCustomerId().equals(customerId)) {
            return "redirect:/customer/services";
        }

        model.addAttribute(
                "service",
                service);

        return "customer/track-service";
    }

    @GetMapping("/bills")
    public String myBills(Authentication authentication, Model model) {
    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();
        List<Bill> bills = billService.getBillsByCustomerId(customerId);
        model.addAttribute("bills", bills);
        return "customer/my-bills";
    }

    @GetMapping("/bill/{id}")
    public String viewBill(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	Long customerId =
    	        user.getId();

        // User not logged in
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        Optional<Bill> billOptional =
                billService.getBillById(id);

        // Bill does not exist
        if (billOptional.isEmpty()) {
            return "redirect:/customer/bills";
        }

        Bill bill =
                billOptional.get();

        // Bill belongs to another customer
        if (!bill.getCustomerId().equals(customerId)) {
            return "redirect:/customer/bills";
        }

        model.addAttribute(
                "bill",
                bill);

        return "customer/view-bill";
    }
}
