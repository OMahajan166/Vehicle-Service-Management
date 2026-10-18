package com.project.controller;

import com.project.config.CustomUserDetails;
import com.project.model.Mechanic;
import com.project.model.Service;
import com.project.service.MechanicService;
import com.project.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/mechanic")
public class MechanicController {

    /** Statuses a mechanic is allowed to set on their own jobs */
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("Accepted", "In Progress", "Completed");

    @Autowired
    private MechanicService mechanicService;

    @Autowired
    private ServiceService serviceService;

    private Optional<Mechanic> currentMechanic(Authentication authentication) {
        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();
        return mechanicService.getMechanicByUserId(user.getId());
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        Optional<Mechanic> mechanicOptional = currentMechanic(authentication);

        if (mechanicOptional.isEmpty()) {
            // Login exists but no mechanic profile is linked to it
            model.addAttribute("noProfile", true);
            return "mechanic/dashboard";
        }

        Mechanic mechanic = mechanicOptional.get();
        List<Service> jobs = serviceService.getServicesByMechanicId(mechanic.getId());

        model.addAttribute("mechanic", mechanic);
        model.addAttribute("jobs", jobs);
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("activeJobs", jobs.stream()
                .filter(s -> "Accepted".equals(s.getStatus())
                        || "In Progress".equals(s.getStatus()))
                .count());
        model.addAttribute("inProgressJobs", jobs.stream()
                .filter(s -> "In Progress".equals(s.getStatus())).count());
        model.addAttribute("completedJobs", jobs.stream()
                .filter(s -> "Completed".equals(s.getStatus())).count());

        return "mechanic/dashboard";
    }

    /** A mechanic can only update jobs assigned to them. */
    @PostMapping("/update-status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Optional<Mechanic> mechanicOptional = currentMechanic(authentication);
        Optional<Service> serviceOptional = serviceService.getServiceById(id);

        if (mechanicOptional.isEmpty() || serviceOptional.isEmpty()
                || !mechanicOptional.get().getId().equals(serviceOptional.get().getMechanicId())) {
            redirectAttributes.addFlashAttribute("error",
                    "That job is not assigned to you.");
            return "redirect:/mechanic/dashboard";
        }

        if (!ALLOWED_STATUSES.contains(status)) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid status: " + status);
            return "redirect:/mechanic/dashboard";
        }

        Service job = serviceOptional.get();
        job.setStatus(status);
        serviceService.updateService(job);

        redirectAttributes.addFlashAttribute("success",
                "Job #" + id + " updated to \"" + status + "\".");
        return "redirect:/mechanic/dashboard";
    }
}
