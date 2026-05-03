package br.com.susUpa.consultas.controller;

import br.com.susUpa.consultas.DTO.AttendanceDTO;
import br.com.susUpa.consultas.domain.Attendance;
import br.com.susUpa.consultas.repository.AttendanceRepository;
import br.com.susUpa.consultas.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final QueueService queueService;

    public AttendanceController(AttendanceRepository attendanceRepository, QueueService queueService) {
        this.attendanceRepository = attendanceRepository;
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<Attendance> create(@RequestBody @Valid AttendanceDTO dto) {
        Attendance attendance = new Attendance();
        attendance.setDoctorName(dto.doctorName());
        attendance.setSpecialty(dto.specialty());
        attendance.setAvailableSlots(dto.availableSlots());
        attendance.setHealthUnit(dto.healthUnit());
        attendance.setCity(dto.city());
        attendance.setAppointmentDate(dto.appointmentDate());
        attendance.setStartTime(dto.startTime() != null ? dto.startTime() : java.time.LocalTime.of(8, 0));
        attendance.setConsultationDurationMinutes(dto.consultationDurationMinutes());
        attendance.setToleranceMinutes(dto.toleranceMinutes() != null ? dto.toleranceMinutes() : 15);
        return ResponseEntity.status(201).body(attendanceRepository.save(attendance));
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> listAll() {
        return ResponseEntity.ok(attendanceRepository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Attendance>> search(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String city
    ) {
        String spec = (specialty != null && !specialty.trim().isEmpty()) ? specialty.trim() : null;
        String cit = (city != null && !city.trim().isEmpty()) ? city.trim() : null;
        return ResponseEntity.ok(attendanceRepository.searchBySpecialtyAndCity(spec, cit));
    }

    @GetMapping("/{id}/remaining")
    public ResponseEntity<Long> getRemainingAppointments(@PathVariable UUID id) {
        return ResponseEntity.ok(queueService.countRemainingForDay(id));
    }
}
