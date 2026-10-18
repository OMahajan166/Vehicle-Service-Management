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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.project.config.CustomUserDetails;


import java.time.LocalDate;
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
    @Autowired
    private UserService userService;

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
        
        model.addAttribute("userName", user.getName());
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

    @GetMapping("/edit-vehicle/{id}")
    public String editVehiclePage(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(id);

        // Only the owner can edit a vehicle
        if (vehicleOptional.isEmpty()
                || !vehicleOptional.get().getCustomerId().equals(user.getId())) {
            return "redirect:/customer/vehicles";
        }

        model.addAttribute("vehicle", vehicleOptional.get());
        return "customer/edit-vehicle";
    }

    @PostMapping("/edit-vehicle/{id}")
    public String editVehicle(
            @PathVariable Long id,
            @Valid @ModelAttribute("vehicle") Vehicle formVehicle,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(id);

        if (vehicleOptional.isEmpty()
                || !vehicleOptional.get().getCustomerId().equals(user.getId())) {
            return "redirect:/customer/vehicles";
        }

        formVehicle.setId(id);

        if (result.hasErrors()) {
            return "customer/edit-vehicle";
        }

        try {
            // Copy editable fields onto the stored vehicle so
            // customerId and createdAt are preserved
            Vehicle vehicle = vehicleOptional.get();
            vehicle.setVehicleNumber(formVehicle.getVehicleNumber());
            vehicle.setCompany(formVehicle.getCompany());
            vehicle.setModel(formVehicle.getModel());
            vehicle.setFuelType(formVehicle.getFuelType());

            vehicleService.updateVehicle(vehicle);

            redirectAttributes.addFlashAttribute("success",
                    "Vehicle " + vehicle.getVehicleNumber() + " updated successfully!");
            return "redirect:/customer/vehicles";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "customer/edit-vehicle";
        }
    }

    @PostMapping("/delete-vehicle/{id}")
    public String deleteVehicle(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(id);

        if (vehicleOptional.isEmpty()
                || !vehicleOptional.get().getCustomerId().equals(user.getId())) {
            return "redirect:/customer/vehicles";
        }

        // A vehicle with bookings keeps its service history — block deletion
        if (!serviceService.getServicesByVehicleId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete this vehicle — it has service bookings in its history.");
            return "redirect:/customer/vehicles";
        }

        vehicleService.deleteVehicle(id);
        redirectAttributes.addFlashAttribute("success", "Vehicle deleted successfully!");
        return "redirect:/customer/vehicles";
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
        model.addAttribute("today", LocalDate.now());
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

        // Past-date rule lives here (booking time only) — NOT on the JPA
        // entity, otherwise old bookings could never be updated later.
        if (service.getServiceDate() != null
                && service.getServiceDate().isBefore(LocalDate.now())) {
            result.rejectValue("serviceDate", "pastDate",
                    "Service date cannot be in the past");
        }

        if (result.hasErrors()) {
            model.addAttribute(
                    "vehicles",
                    vehicleService.getVehiclesByCustomerId(customerId));
            model.addAttribute("today", LocalDate.now());

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

    /** Pay a bill (simulated payment — no real gateway). Only the bill's owner can pay it. */
    @PostMapping("/bill/{id}/pay")
    public String payBill(
            @PathVariable Long id,
            @RequestParam(defaultValue = "UPI") String paymentMethod,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();
        Long customerId = user.getId();

        Optional<Bill> billOptional = billService.getBillById(id);

        if (billOptional.isEmpty()
                || !billOptional.get().getCustomerId().equals(customerId)) {
            redirectAttributes.addFlashAttribute("error", "Bill not found.");
            return "redirect:/customer/bills";
        }

        if (billService.payBill(billOptional.get(), paymentMethod)) {
            redirectAttributes.addFlashAttribute("success",
                    "Payment successful! Bill #" + id + " is now Paid.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Bill #" + id + " is already paid.");
        }
        return "redirect:/customer/bill/" + id;
    }

    /** Printable invoice for a bill. Only the bill's owner can view it. */
    @GetMapping("/bill/{id}/print")
    public String printBill(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();
        Long customerId = user.getId();

        Optional<Bill> billOptional = billService.getBillById(id);

        if (billOptional.isEmpty()
                || !billOptional.get().getCustomerId().equals(customerId)) {
            return "redirect:/customer/bills";
        }

        Bill bill = billOptional.get();
        model.addAttribute("bill", bill);

        userService.getUserById(bill.getCustomerId())
                .ifPresent(customer -> model.addAttribute("customer", customer));

        serviceService.getServiceById(bill.getServiceId()).ifPresent(service -> {
            model.addAttribute("service", service);
            vehicleService.getVehicleById(service.getVehicleId())
                    .ifPresent(vehicle -> model.addAttribute("vehicle", vehicle));
        });

        return "customer/print-bill";
    }
}
