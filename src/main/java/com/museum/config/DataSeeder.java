package com.museum.config;

import com.museum.model.Admin;
import com.museum.model.UserRole;
import com.museum.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds a default Admin account on startup.
 * Default: admin@museum.com / admin123
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;

    public DataSeeder(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.findByEmail("admin@museum.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setName("System Admin");
            admin.setEmail("admin@museum.com");
            admin.setPassword("admin123");
            admin.setRole(UserRole.ADMIN);
            adminRepository.save(admin);
            System.out.println("✅ Default admin created: admin@museum.com / admin123");
        }
    }
}
