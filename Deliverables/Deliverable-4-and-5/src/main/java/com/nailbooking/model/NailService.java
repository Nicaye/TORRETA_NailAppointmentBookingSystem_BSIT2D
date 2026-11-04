package com.nailbooking.model;

public record NailService(
        int id,
        String serviceName,
        double price,
        int durationMinutes
) {
}
