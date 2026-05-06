package com.project.controller;

import com.project.model.User;
import com.project.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        Optional<User> existingUser = userService.getUserByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }
        userService.registerUser(user);
        model.addAttribute("success", "Registration successful! Please login.");
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> user = userService.loginUser(email, password);
        if (user.isPresent()) {
            session.setAttribute("userId", user.get().getId());
            session.setAttribute("userName", user.get().getName());
            session.setAttribute("userEmail", user.get().getEmail());
            session.setAttribute("userRole", user.get().getRole());
            
            if ("ADMIN".equals(user.get().getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/customer/dashboard";
            }
        }
        model.addAttribute("error", "Invalid email or password!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
