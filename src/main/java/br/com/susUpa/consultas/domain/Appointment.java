package br.com.susUpa.consultas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tb_appointment")
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID patientId;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    private Integer queuePosition;
    private LocalTime scheduledTime;
    private LocalDateTime checkinTime;
    private LocalDateTime serviceStartTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @PrePersist
    public void prePersist() {
        this.status = AppointmentStatus.SCHEDULED;
    }
}
