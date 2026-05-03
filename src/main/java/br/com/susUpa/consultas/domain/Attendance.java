package br.com.susUpa.consultas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tb_attendance")
@Getter
@Setter
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String doctorName;
    private String specialty;
    private Integer availableSlots;
    private String healthUnit;
    private String city;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private Integer consultationDurationMinutes;
    private Integer toleranceMinutes;
}
