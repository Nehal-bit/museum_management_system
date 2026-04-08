package com.museum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @PrePersist
    protected void prePersist() {
        this.setRole(UserRole.ADMIN);
    }
}
