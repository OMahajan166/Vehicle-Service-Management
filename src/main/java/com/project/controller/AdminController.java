package com.project.controller;

import com.project.model.Bill;
import com.project.model.Mechanic;
import com.project.model.Service;
import com.project.model.User;
import com.project.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private BillService billService;
    @Autowired
    private MechanicService mechanicService;
    @Autowired
    private ContactService contactService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/auth/login";
        }
        
        List<User> customers = userService.getUserByEmail("customer".substring(0,0)).map(u -> u).stream().toList();
        List<Mechanic> mechanics = mechanicService.getAllMechanics();
        List<Service> services = serviceService.getAllServices();
        
        model.addAttribute("totalCustomers", 0);
        model.addAttribute("totalMechanics", mechanics.size());
        model.addAttribute("pendingServices", serviceService.getPendingServicesCount());
        model.addAttribute("completedServices", serviceService.getCompletedServicesCount());
        model.addAttribute("totalRevenue", billService.getTotalRevenue());
        model.addAttribute("totalServices", services.size());
        
        return "admin/dashboard";
    }

    @GetMapping("/customers")
    public String manageCustomers(Model model) {
        model.addAttribute("customers", new java.util.ArrayList<>());
        return "admin/customers";
    }

    @GetMapping("/mechanics")
    public String manageMechanics(Model model) {
        List<Mechanic> mechanics = mechanicService.getAllMechanics();
        model.addAttribute("mechanics", mechanics);
        model.addAttribute("newMechanic", new Mechanic());
        return "admin/mechanics";
    }

    @PostMapping("/add-mechanic")
    public String addMechanic(@ModelAttribute Mechanic mechanic) {
        mechanicService.addMechanic(mechanic);
        return "redirect:/admin/mechanics";
    }

    @GetMapping("/services")
    public String manageServices(Model model) {
        List<Service> services = serviceService.getAllServices();
        List<Mechanic> mechanics = mechanicService.getAllMechanics();
        model.addAttribute("services", services);
        model.addAttribute("mechanics", mechanics);
        return "admin/services";
    }

    @PostMapping("/update-service-status/{id}")
    public String updateServiceStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Service> service = serviceService.getServiceById(id);
        if (service.isPresent()) {
            Service s = service.get();
            s.setStatus(status);
            serviceService.updateService(s);
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/assign-mechanic/{id}")
    public String assignMechanic(@PathVariable Long id, @RequestParam Long mechanicId) {
        Optional<Service> service = serviceService.getServiceById(id);
        if (service.isPresent()) {
            Service s = service.get();
            s.setMechanicId(mechanicId);
            s.setStatus("Accepted");
            serviceService.updateService(s);
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/bills")
    public String manageBills(Model model) {
        List<Bill> bills = billService.getAllBills();
        model.addAttribute("bills", bills);
        return "admin/bills";
    }

    @GetMapping("/generate-bill")
    public String generateBillPage(Model model) {
        model.addAttribute("bill", new Bill());
        model.addAttribute("services", serviceService.getAllServices());
        return "admin/generate-bill";
    }

    @PostMapping("/generate-bill")
    public String generateBill(@ModelAttribute Bill bill) {
        billService.generateBill(bill);
        return "redirect:/admin/bills";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalRevenue", billService.getTotalRevenue());
        model.addAttribute("totalServices", serviceService.getAllServices().size());
        model.addAttribute("completedServices", serviceService.getCompletedServicesCount());
        model.addAttribute("pendingServices", serviceService.getPendingServicesCount());
        return "admin/reports";
    }

    @GetMapping("/contacts")
    public String viewContacts(Model model) {
        model.addAttribute("contacts", contactService.getAllContacts());
        return "admin/contacts";
    }
}
