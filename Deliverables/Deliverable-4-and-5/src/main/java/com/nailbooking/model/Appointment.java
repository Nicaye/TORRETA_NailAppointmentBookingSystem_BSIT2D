package com.nailbooking.model;

public record Appointment(
        int id,
        String customerName,
        String contactNumber,
        int serviceId,
        String appointmentDate,
        String appointmentTime,
        String status
) {
}
