package br.com.susUpa.consultas.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAppointmentDTO(

        @NotNull
        UUID patientId,

        @NotNull
        UUID attendanceId,

        @NotNull
        LocalDate appointmentDate
) {}
