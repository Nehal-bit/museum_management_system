package com.museum.service;

import com.museum.model.TicketType;
import org.springframework.stereotype.Service;

@Service
public class TicketPricingService {
    public double getPrice(TicketType type) {
        return switch (type) {
            case ADULT   -> 500.00;
            case CHILD   -> 200.00;
            case STUDENT -> 150.00;
            case VIP     -> 1500.00;
        };
    }
}
