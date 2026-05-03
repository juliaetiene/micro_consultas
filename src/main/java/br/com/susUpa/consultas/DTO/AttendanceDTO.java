package br.com.susUpa.consultas.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceDTO(

        @NotEmpty
        String doctorName,

        @NotEmpty
        String specialty,

        @NotNull
        Integer availableSlots,

        @NotEmpty
        String healthUnit,

        @NotEmpty
        String city,

        LocalDate appointmentDate,

        LocalTime startTime,

        @NotNull
        Integer consultationDurationMinutes,

        @NotNull
        Integer toleranceMinutes
) {}
