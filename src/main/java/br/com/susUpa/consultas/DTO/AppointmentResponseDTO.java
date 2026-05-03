package br.com.susUpa.consultas.DTO;

import br.com.susUpa.consultas.domain.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponseDTO(
        UUID id,
        UUID patientId,
        String doctorName,
        String specialty,
        String healthUnit,
        String city,
        LocalDate appointmentDate,
        LocalTime scheduledTime,
        Integer queuePosition,
        AppointmentStatus status,
        Integer patientsAhead
) {}
