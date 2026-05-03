package br.com.susUpa.consultas.DTO;

import br.com.susUpa.consultas.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusDTO(

        @NotNull
        AppointmentStatus status
) {}
