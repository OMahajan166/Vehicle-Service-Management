package com.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Enter a valid 10 digit phone number"
    )
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Contact() {
    }

    public Contact(Long id, String name, String email,
                   String phone, String message,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name =
                name != null ? name.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email =
                email != null
                        ? email.trim().toLowerCase()
                        : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone =
                phone != null ? phone.trim() : null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message =
                message != null ? message.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}