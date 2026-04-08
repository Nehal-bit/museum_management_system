package com.museum.dto;

import com.museum.model.UserRole;

public class RegisterDTO {
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private String phone;
    private String specialization;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole v) { this.role = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String v) { this.specialization = v; }
}
