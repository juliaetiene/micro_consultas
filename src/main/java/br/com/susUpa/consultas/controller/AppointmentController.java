package br.com.susUpa.consultas.controller;

import br.com.susUpa.consultas.DTO.AppointmentResponseDTO;
import br.com.susUpa.consultas.DTO.CreateAppointmentDTO;
import br.com.susUpa.consultas.DTO.QueueItemDTO;
import br.com.susUpa.consultas.DTO.UpdateAppointmentStatusDTO;
import br.com.susUpa.consultas.domain.Appointment;
import br.com.susUpa.consultas.domain.AppointmentStatus;
import br.com.susUpa.consultas.domain.Attendance;
import br.com.susUpa.consultas.repository.AppointmentRepository;
import br.com.susUpa.consultas.repository.AttendanceRepository;
import br.com.susUpa.consultas.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final QueueService queueService;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                  AttendanceRepository attendanceRepository,
                                  QueueService queueService) {
        this.appointmentRepository = appointmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateAppointmentDTO dto) {
        Attendance attendance = attendanceRepository.findById(dto.attendanceId()).orElse(null);
        if (attendance == null) {
            return ResponseEntity.badRequest().body("Atendimento não encontrado.");
        }
        if (attendance.getAvailableSlots() <= 0) {
            return ResponseEntity.badRequest().body("Não há vagas disponíveis.");
        }

        int position = queueService.getNextQueuePosition(dto.attendanceId());

        Appointment appointment = new Appointment();
        appointment.setPatientId(dto.patientId());
        appointment.setAttendance(attendance);
        appointment.setQueuePosition(position);
        appointment.setScheduledTime(queueService.calculateScheduledTime(attendance, position));

        attendance.setAvailableSlots(attendance.getAvailableSlots() - 1);
        attendanceRepository.save(attendance);

        return ResponseEntity.status(201).body(appointmentRepository.save(appointment));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
    }

    @GetMapping("/patient/{patientId}/search")
    public ResponseEntity<List<Appointment>> searchByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorName
    ) {
        String spec = (specialty != null && !specialty.trim().isEmpty()) ? specialty.trim() : null;
        String doc = (doctorName != null && !doctorName.trim().isEmpty()) ? doctorName.trim() : null;
        return ResponseEntity.ok(appointmentRepository.searchByPatient(patientId, spec, doc));
    }

    @GetMapping("/{id}/info")
    public ResponseEntity<?> getInfo(@PathVariable UUID id) {
        return appointmentRepository.findById(id).map(apt -> {
            int ahead = queueService.countPatientsAhead(apt.getAttendance().getId(), apt.getQueuePosition());
            AppointmentResponseDTO response = new AppointmentResponseDTO(
                    apt.getId(),
                    apt.getPatientId(),
                    apt.getAttendance().getDoctorName(),
                    apt.getAttendance().getSpecialty(),
                    apt.getAttendance().getHealthUnit(),
                    apt.getAttendance().getCity(),
                    apt.getAttendance().getAppointmentDate(),
                    apt.getScheduledTime(),
                    apt.getQueuePosition(),
                    apt.getStatus(),
                    ahead
            );
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/queue/{attendanceId}")
    public ResponseEntity<List<QueueItemDTO>> getQueue(@PathVariable UUID attendanceId) {
        List<QueueItemDTO> queue = appointmentRepository
                .findByAttendanceIdOrderByQueuePosition(attendanceId)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.ABSENT)
                .map(a -> new QueueItemDTO(
                        a.getId(),
                        a.getPatientId(),
                        a.getQueuePosition(),
                        a.getScheduledTime(),
                        a.getStatus()
                ))
                .toList();
        return ResponseEntity.ok(queue);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody @Valid UpdateAppointmentStatusDTO dto) {
        return appointmentRepository.findById(id).map(appointment -> {
            AppointmentStatus newStatus = dto.status();
            Attendance attendance = appointment.getAttendance();

            if (newStatus == AppointmentStatus.CONCLUDED) {
                return ResponseEntity.badRequest().body("O status CONCLUÍDO é definido automaticamente pelo sistema após 30 minutos.");
            }

            if (newStatus == AppointmentStatus.WAITING) {
                appointment.setCheckinTime(LocalDateTime.now());
            }

            if (newStatus == AppointmentStatus.IN_SERVICE) {
                appointment.setServiceStartTime(LocalDateTime.now());
            }

            if (newStatus == AppointmentStatus.ABSENT || newStatus == AppointmentStatus.CANCELLED) {
                int removedPosition = appointment.getQueuePosition();
                attendance.setAvailableSlots(attendance.getAvailableSlots() + 1);
                attendanceRepository.save(attendance);
                appointment.setStatus(newStatus);
                appointmentRepository.save(appointment);
                queueService.recalculateQueueAfterRemoval(attendance, removedPosition);
                return ResponseEntity.ok(appointment);
            }

            appointment.setStatus(newStatus);
            return ResponseEntity.ok(appointmentRepository.save(appointment));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        return appointmentRepository.findById(id).map(appointment -> {
            if (appointment.getStatus() == AppointmentStatus.CONCLUDED || appointment.getStatus() == AppointmentStatus.IN_SERVICE) {
                return ResponseEntity.badRequest().body("Não é possível cancelar uma consulta em andamento ou concluída.");
            }
            int removedPosition = appointment.getQueuePosition();
            Attendance attendance = appointment.getAttendance();
            attendance.setAvailableSlots(attendance.getAvailableSlots() + 1);
            attendanceRepository.save(attendance);
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            queueService.recalculateQueueAfterRemoval(attendance, removedPosition);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(java.util.Map.of(
            "total", appointmentRepository.count(),
            "today", appointmentRepository.countByAttendance_AppointmentDate(java.time.LocalDate.now())
        ));
    }
}
