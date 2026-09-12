package com.example.drivehealth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Safe public representation of an application User.
 */
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;
    private List<GoogleAccountResponse> accounts = new ArrayList<>();

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String name, LocalDateTime createdAt, List<GoogleAccountResponse> accounts) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
        this.accounts = accounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<GoogleAccountResponse> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<GoogleAccountResponse> accounts) {
        this.accounts = accounts;
    }
}
