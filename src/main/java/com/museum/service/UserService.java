package com.museum.service;

import com.museum.dto.LoginDTO;
import com.museum.dto.RegisterDTO;
import com.museum.model.*;
import com.museum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VisitorRepository visitorRepository;
    private final GuideRepository guideRepository;
    private final AdminRepository adminRepository;

    public UserService(UserRepository userRepository,
                       VisitorRepository visitorRepository,
                       GuideRepository guideRepository,
                       AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.visitorRepository = visitorRepository;
        this.guideRepository = guideRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public User register(RegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered: " + dto.getEmail());
        }
        if (dto.getRole() == UserRole.VISITOR) {
            Visitor v = new Visitor();
            v.setName(dto.getName());
            v.setEmail(dto.getEmail());
            v.setPassword(dto.getPassword());
            v.setPhone(dto.getPhone());
            return visitorRepository.save(v);
        } else if (dto.getRole() == UserRole.GUIDE) {
            Guide g = new Guide();
            g.setName(dto.getName());
            g.setEmail(dto.getEmail());
            g.setPassword(dto.getPassword());
            g.setSpecialization(dto.getSpecialization());
            return guideRepository.save(g);
        } else {
            throw new RuntimeException("Invalid role for registration.");
        }
    }

    @Transactional(readOnly = true)
    public User login(LoginDTO dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));
    }

    @Transactional(readOnly = true) public List<User> getAllUsers() { return userRepository.findAll(); }

    @Transactional
    public void deleteGuide(Long guideId) {
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found: " + guideId));
        // Check for active (PENDING or CONFIRMED) assignments
        boolean hasActive = guide.getAssignments().stream()
                .anyMatch(a -> "PENDING".equals(a.getStatus()) || "CONFIRMED".equals(a.getStatus()));
        if (hasActive) {
            throw new RuntimeException(
                "Cannot delete guide with active assignments. " +
                "Reassign or cancel those bookings first.");
        }
        guideRepository.delete(guide);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) { return userRepository.findByRole(role); }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}
