package com.project.controller;

import com.project.model.Bill;
import com.project.model.Mechanic;
import com.project.model.Service;
import com.project.model.User;
import com.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.project.config.CustomUserDetails;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
    public String dashboard(Authentication authentication, Model model) {
    	CustomUserDetails user =
    	        (CustomUserDetails)
    	                authentication.getPrincipal();

    	String userRole =
    	        user.getRole();
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/auth/login";
        }
        
        List<User> customers =
                userService.getUsersByRole(
                        "CUSTOMER");
        List<Mechanic> mechanics = mechanicService.getAllMechanics();
        List<Service> services = serviceService.getAllServices();
        
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("totalMechanics", mechanics.size());
        model.addAttribute("pendingServices", serviceService.getPendingServicesCount());
        model.addAttribute("completedServices", serviceService.getCompletedServicesCount());
        model.addAttribute("totalRevenue", billService.getTotalRevenue());
        model.addAttribute("totalServices", services.size());
        
        return "admin/dashboard";
    }

    /** Case-insensitive "field contains keyword" helper for list filtering. */
    private static boolean contains(String field, String lowerKeyword) {
        return field != null && field.toLowerCase(java.util.Locale.ROOT).contains(lowerKeyword);
    }

    @GetMapping("/customers")
    public String manageCustomers(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<User> customers = userService.getUsersByRole("CUSTOMER");

        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim().toLowerCase(java.util.Locale.ROOT);
            customers = customers.stream()
                    .filter(c -> contains(c.getName(), q)
                            || contains(c.getEmail(), q)
                            || contains(c.getPhone(), q))
                    .toList();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("keyword", keyword);

        return "admin/customers";
    }

    @GetMapping("/mechanics")
    public String manageMechanics(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Mechanic> mechanics = mechanicService.getAllMechanics();

        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim().toLowerCase(java.util.Locale.ROOT);
            mechanics = mechanics.stream()
                    .filter(m -> contains(m.getName(), q)
                            || contains(m.getSpecialization(), q)
                            || contains(m.getEmail(), q)
                            || contains(m.getPhone(), q))
                    .toList();
        }

        model.addAttribute("mechanics", mechanics);
        model.addAttribute("newMechanic", new Mechanic());
        model.addAttribute("keyword", keyword);
        return "admin/mechanics";
    }

    @PostMapping("/add-mechanic")
    public String addMechanic(
            @ModelAttribute Mechanic mechanic,
            @RequestParam String password,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        // The mechanic's email becomes their login username — it must be unique
        if (userService.getUserByEmail(mechanic.getEmail()).isPresent()
                || mechanicService.getAllMechanics().stream()
                        .anyMatch(m -> m.getEmail().equalsIgnoreCase(mechanic.getEmail()))) {
            redirectAttributes.addFlashAttribute("error",
                    "A user/mechanic with email " + mechanic.getEmail() + " already exists.");
            return "redirect:/admin/mechanics";
        }

        try {
            // 1) Create the login account (role MECHANIC)
            com.project.model.User account = new com.project.model.User();
            account.setName(mechanic.getName());
            account.setEmail(mechanic.getEmail());
            account.setPhone(mechanic.getPhone());
            account.setPassword(password);
            account = userService.createAccount(account, "MECHANIC");

            // 2) Create the mechanic profile linked to that account
            mechanic.setUserId(account.getId());
            mechanicService.addMechanic(mechanic);

            redirectAttributes.addFlashAttribute("success",
                    "Mechanic " + mechanic.getName() + " added. They can now log in with "
                            + mechanic.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Could not add mechanic: " + e.getMessage());
        }
        return "redirect:/admin/mechanics";
    }

    @GetMapping("/edit-mechanic/{id}")
    public String editMechanicPage(@PathVariable Long id, Model model) {
        Optional<Mechanic> mechanic = mechanicService.getMechanicById(id);
        if (mechanic.isEmpty()) {
            return "redirect:/admin/mechanics";
        }
        model.addAttribute("mechanic", mechanic.get());
        return "admin/edit-mechanic";
    }

    @PostMapping("/edit-mechanic/{id}")
    public String editMechanic(
            @PathVariable Long id,
            @ModelAttribute Mechanic formMechanic,
            @RequestParam(required = false) String newPassword,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Optional<Mechanic> mechanicOptional = mechanicService.getMechanicById(id);
        if (mechanicOptional.isEmpty()) {
            return "redirect:/admin/mechanics";
        }

        // Copy editable fields onto the stored mechanic (keeps id/createdAt)
        Mechanic mechanic = mechanicOptional.get();
        mechanic.setName(formMechanic.getName());
        mechanic.setSpecialization(formMechanic.getSpecialization());
        mechanic.setPhone(formMechanic.getPhone());
        mechanic.setEmail(formMechanic.getEmail());
        mechanic.setExperience(formMechanic.getExperience());

        mechanicService.updateMechanic(mechanic);

        // Older mechanics (added before logins existed) have no account:
        // setting a password here creates their login account
        if (mechanic.getUserId() == null
                && newPassword != null && !newPassword.isBlank()) {
            com.project.model.User account = new com.project.model.User();
            account.setName(mechanic.getName());
            account.setEmail(mechanic.getEmail());
            account.setPhone(mechanic.getPhone());
            account.setPassword(newPassword);
            account = userService.createAccount(account, "MECHANIC");
            mechanic.setUserId(account.getId());
            mechanicService.updateMechanic(mechanic);
            newPassword = null; // account created with this password already
        }

        // Keep the linked login account in sync; optionally reset its password
        if (mechanic.getUserId() != null) {
            userService.getUserById(mechanic.getUserId()).ifPresent(account -> {
                account.setName(mechanic.getName());
                account.setEmail(mechanic.getEmail());
                account.setPhone(mechanic.getPhone());
                userService.updateUser(account);
            });
            if (newPassword != null && !newPassword.isBlank()) {
                userService.changePassword(mechanic.getUserId(), newPassword);
            }
        }

        redirectAttributes.addFlashAttribute("success",
                "Mechanic " + mechanic.getName() + " updated successfully!");
        return "redirect:/admin/mechanics";
    }

    @PostMapping("/delete-mechanic/{id}")
    public String deleteMechanic(
            @PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Optional<Mechanic> mechanicOptional = mechanicService.getMechanicById(id);
        if (mechanicOptional.isEmpty()) {
            return "redirect:/admin/mechanics";
        }

        // Block deletion while the mechanic has unfinished jobs
        long activeJobs = serviceService.getServicesByMechanicId(id).stream()
                .filter(s -> !"Completed".equals(s.getStatus())
                        && !"Cancelled".equals(s.getStatus()))
                .count();

        if (activeJobs > 0) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete " + mechanicOptional.get().getName()
                            + " — they have " + activeJobs + " active job(s). "
                            + "Reassign or complete those services first.");
            return "redirect:/admin/mechanics";
        }

        Long linkedUserId = mechanicOptional.get().getUserId();
        mechanicService.deleteMechanic(id);
        // Remove the linked login account so it can't be used anymore
        if (linkedUserId != null) {
            userService.deleteUser(linkedUserId);
        }
        redirectAttributes.addFlashAttribute("success", "Mechanic deleted successfully!");
        return "redirect:/admin/mechanics";
    }

    @GetMapping("/services")
    public String manageServices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Service> services = serviceService.getAllServices();

        if (status != null && !status.isBlank()) {
            services = services.stream()
                    .filter(s -> s.getStatus() != null
                            && s.getStatus().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim().toLowerCase(java.util.Locale.ROOT);
            services = services.stream()
                    .filter(s -> contains(s.getServiceType(), q)
                            || contains(s.getDescription(), q)
                            || String.valueOf(s.getId()).equals(q)
                            || String.valueOf(s.getCustomerId()).equals(q)
                            || String.valueOf(s.getVehicleId()).equals(q))
                    .toList();
        }

        model.addAttribute("services", services);
        model.addAttribute("mechanics", mechanicService.getAllMechanics());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
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
    public String manageBills(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Bill> bills = billService.getAllBills();

        if (status != null && !status.isBlank()) {
            bills = bills.stream()
                    .filter(b -> b.getStatus() != null
                            && b.getStatus().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            bills = bills.stream()
                    .filter(b -> String.valueOf(b.getId()).equals(q)
                            || String.valueOf(b.getServiceId()).equals(q)
                            || String.valueOf(b.getCustomerId()).equals(q))
                    .toList();
        }

        model.addAttribute("bills", bills);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin/bills";
    }

    @GetMapping("/generate-bill")
    public String generateBillPage(Model model) {
        model.addAttribute("bill", new Bill());
        model.addAttribute("services", serviceService.getAllServices());
        return "admin/generate-bill";
    }

    @PostMapping("/generate-bill")
    public String generateBill(@ModelAttribute Bill bill,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // Derive the customer from the selected service — never trust a
        // hand-typed customer ID, which could bill the wrong customer.
        Optional<Service> service = serviceService.getServiceById(bill.getServiceId());
        if (service.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a valid service.");
            return "redirect:/admin/generate-bill";
        }
        // One bill per service — block duplicates.
        if (billService.getBillByServiceId(bill.getServiceId()).isPresent()) {
            redirectAttributes.addFlashAttribute("error",
                    "A bill has already been generated for service #" + bill.getServiceId());
            return "redirect:/admin/generate-bill";
        }
        bill.setCustomerId(service.get().getCustomerId());
        billService.generateBill(bill);
        return "redirect:/admin/bills";
    }

    @GetMapping("/reports")
    public String reports(Model model) {

        List<Bill> bills = billService.getAllBills();
        List<Service> allServices = serviceService.getAllServices();

        double paidRevenue = bills.stream()
                .filter(b -> "Paid".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(b -> b.getTotalAmount() == null ? 0 : b.getTotalAmount())
                .sum();
        double pendingAmount = bills.stream()
                .filter(b -> !"Paid".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(b -> b.getTotalAmount() == null ? 0 : b.getTotalAmount())
                .sum();

        // ---- Revenue by month (oldest -> newest) ----
        Map<YearMonth, Double> byMonth = new TreeMap<>();
        for (Bill b : bills) {
            if (b.getCreatedAt() != null && b.getTotalAmount() != null) {
                byMonth.merge(YearMonth.from(b.getCreatedAt()), b.getTotalAmount(), Double::sum);
            }
        }
        double maxMonthly = byMonth.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(0);

        DateTimeFormatter monthLabel =
                DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        byMonth.forEach((month, total) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", month.atDay(1).format(monthLabel));
            row.put("total", total);
            row.put("percent", maxMonthly > 0 ? (total / maxMonthly) * 100 : 0);
            monthlyRevenue.add(row);
        });

        // ---- Services by status ----
        List<String> statuses =
                List.of("Pending", "Accepted", "In Progress", "Completed", "Cancelled");
        List<Map<String, Object>> statusCounts = new ArrayList<>();
        for (String status : statuses) {
            long count = allServices.stream()
                    .filter(s -> status.equalsIgnoreCase(s.getStatus()))
                    .count();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", status);
            row.put("count", count);
            row.put("percent", allServices.isEmpty()
                    ? 0 : (count * 100.0 / allServices.size()));
            statusCounts.add(row);
        }

        model.addAttribute("totalRevenue", billService.getTotalRevenue());
        model.addAttribute("paidRevenue", paidRevenue);
        model.addAttribute("pendingAmount", pendingAmount);
        model.addAttribute("totalServices", allServices.size());
        model.addAttribute("completedServices", serviceService.getCompletedServicesCount());
        model.addAttribute("pendingServices", serviceService.getPendingServicesCount());
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("statusCounts", statusCounts);
        return "admin/reports";
    }

    /** Quote one CSV cell (handles commas/quotes safely). */
    private static String csvCell(Object value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + String.valueOf(value).replace("\"", "\"\"") + "\"";
    }

    private static String csvRow(Object... cells) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                row.append(',');
            }
            row.append(csvCell(cells[i]));
        }
        return row.append("\n").toString();
    }

    /** Download report data as CSV: /admin/reports/export?type=bills|services|customers */
    @GetMapping(value = "/reports/export", produces = "text/csv")
    @ResponseBody
    public ResponseEntity<String> exportCsv(
            @RequestParam(defaultValue = "bills") String type) {

        StringBuilder csv = new StringBuilder();
        String filename;

        switch (type.toLowerCase(java.util.Locale.ROOT)) {
            case "services" -> {
                filename = "services-report.csv";
                csv.append(csvRow("Service ID", "Customer ID", "Vehicle ID", "Mechanic ID",
                        "Service Type", "Description", "Service Date", "Status", "Created At"));
                for (Service s : serviceService.getAllServices()) {
                    csv.append(csvRow(s.getId(), s.getCustomerId(), s.getVehicleId(),
                            s.getMechanicId(), s.getServiceType(), s.getDescription(),
                            s.getServiceDate(), s.getStatus(), s.getCreatedAt()));
                }
            }
            case "customers" -> {
                filename = "customers-report.csv";
                csv.append(csvRow("Customer ID", "Name", "Email", "Phone", "Registered On"));
                for (User u : userService.getUsersByRole("CUSTOMER")) {
                    csv.append(csvRow(u.getId(), u.getName(), u.getEmail(),
                            u.getPhone(), u.getCreatedAt()));
                }
            }
            default -> {
                filename = "bills-report.csv";
                csv.append(csvRow("Bill ID", "Service ID", "Customer ID", "Labor Cost",
                        "Spare Parts", "Tax", "Total Amount", "Status",
                        "Payment Method", "Paid At", "Created At"));
                for (Bill b : billService.getAllBills()) {
                    csv.append(csvRow(b.getId(), b.getServiceId(), b.getCustomerId(),
                            b.getLaborCost(), b.getSpareParts(), b.getTax(),
                            b.getTotalAmount(), b.getStatus(), b.getPaymentMethod(),
                            b.getPaidAt(), b.getCreatedAt()));
                }
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    @GetMapping("/contacts")
    public String viewContacts(Model model) {
        model.addAttribute("contacts", contactService.getAllContacts());
        return "admin/contacts";
    }
}
