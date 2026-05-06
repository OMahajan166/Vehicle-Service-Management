package com.project.controller;

import com.project.model.Contact;
import com.project.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contact")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @GetMapping
    public String contactPage(Model model) {
        model.addAttribute("contact", new Contact());
        return "contact";
    }

    @PostMapping
    public String submitContact(@ModelAttribute Contact contact, Model model) {
        contactService.saveContact(contact);
        model.addAttribute("success", "Your message has been sent successfully!");
        model.addAttribute("contact", new Contact());
        return "contact";
    }
}
