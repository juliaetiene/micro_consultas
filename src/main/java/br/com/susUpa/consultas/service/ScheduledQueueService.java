package br.com.susUpa.consultas.service;

import br.com.susUpa.consultas.domain.Appointment;
import br.com.susUpa.consultas.domain.AppointmentStatus;
import br.com.susUpa.consultas.repository.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduledQueueService {

    private final AppointmentRepository appointmentRepository;
    private final QueueService queueService;

    public ScheduledQueueService(AppointmentRepository appointmentRepository, QueueService queueService) {
        this.appointmentRepository = appointmentRepository;
        this.queueService = queueService;
    }

    @Scheduled(fixedDelay = 60000)
    public void processAutoConclusion() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);

        List<Appointment> waiting = appointmentRepository.findByStatus(AppointmentStatus.WAITING);

        for (Appointment apt : waiting) {
            if (apt.getCheckinTime() != null && apt.getCheckinTime().isBefore(cutoff)) {
                apt.setStatus(AppointmentStatus.CONCLUDED);
                appointmentRepository.save(apt);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void processAutoAbsent() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        List<Appointment> candidates = appointmentRepository.findByAttendance_AppointmentDateAndStatusIn(
                today,
                List.of(AppointmentStatus.SCHEDULED)
        );

        for (Appointment apt : candidates) {
            if (apt.getScheduledTime() == null) {
                continue;
            }

            int tolerance = apt.getAttendance().getToleranceMinutes() != null ? apt.getAttendance().getToleranceMinutes() : 15;
            LocalTime deadline = apt.getScheduledTime().plusMinutes(tolerance);

            if (now.isAfter(deadline)) {
                int removedPosition = apt.getQueuePosition();
                apt.setStatus(AppointmentStatus.ABSENT);
                apt.getAttendance().setAvailableSlots(apt.getAttendance().getAvailableSlots() + 1);
                appointmentRepository.save(apt);
                queueService.recalculateQueueAfterRemoval(apt.getAttendance(), removedPosition);
            }
        }
    }
}
