package br.com.susUpa.consultas.DTO;

import br.com.susUpa.consultas.domain.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record QueueItemDTO(
        UUID appointmentId,
        UUID patientId,
        Integer queuePosition,
        LocalTime scheduledTime,
        AppointmentStatus status
) {}
