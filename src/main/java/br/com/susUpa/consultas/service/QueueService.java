package br.com.susUpa.consultas.service;

import br.com.susUpa.consultas.domain.Appointment;
import br.com.susUpa.consultas.domain.AppointmentStatus;
import br.com.susUpa.consultas.domain.Attendance;
import br.com.susUpa.consultas.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class QueueService {

    private final AppointmentRepository appointmentRepository;

    public QueueService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public LocalTime calculateScheduledTime(Attendance attendance, int queuePosition) {
        int totalMinutes = (queuePosition - 1) * attendance.getConsultationDurationMinutes();
        return attendance.getStartTime().plusMinutes(totalMinutes);
    }

    public int getNextQueuePosition(UUID attendanceId) {
        List<Appointment> active = appointmentRepository.findByAttendanceIdOrderByQueuePosition(attendanceId)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.ABSENT)
                .toList();
        return active.size() + 1;
    }

    public void recalculateQueueAfterRemoval(Attendance attendance, int removedPosition) {
        List<Appointment> toShift = appointmentRepository
                .findByAttendanceIdAndQueuePositionGreaterThanOrderByQueuePosition(attendance.getId(), removedPosition)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.ABSENT)
                .toList();

        for (Appointment apt : toShift) {
            int newPosition = apt.getQueuePosition() - 1;
            apt.setQueuePosition(newPosition);
            apt.setScheduledTime(calculateScheduledTime(attendance, newPosition));
            appointmentRepository.save(apt);
        }
    }

    public int countPatientsAhead(UUID attendanceId, int queuePosition) {
        return (int) appointmentRepository.findByAttendanceIdOrderByQueuePosition(attendanceId)
                .stream()
                .filter(a -> a.getQueuePosition() < queuePosition
                        && a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getStatus() != AppointmentStatus.ABSENT
                        && a.getStatus() != AppointmentStatus.CONCLUDED)
                .count();
    }

    public long countRemainingForDay(UUID attendanceId) {
        return appointmentRepository.countByAttendanceIdAndStatusIn(attendanceId,
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.WAITING, AppointmentStatus.IN_SERVICE));
    }
}
