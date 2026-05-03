package br.com.susUpa.consultas.repository;

import br.com.susUpa.consultas.domain.Appointment;
import br.com.susUpa.consultas.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientId(UUID patientId);

    long countByAttendance_AppointmentDate(java.time.LocalDate date);

    List<Appointment> findByAttendanceIdOrderByQueuePosition(UUID attendanceId);

    List<Appointment> findByAttendanceId(UUID attendanceId);

    List<Appointment> findByAttendanceIdAndQueuePositionGreaterThanOrderByQueuePosition(UUID attendanceId, int queuePosition);

    long countByAttendanceIdAndStatusIn(UUID attendanceId, List<AppointmentStatus> statuses);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByAttendance_AppointmentDateAndStatusIn(
            java.time.LocalDate date,
            List<AppointmentStatus> statuses
    );

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
            "AND (:specialty IS NULL OR LOWER(a.attendance.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))) " +
            "AND (:doctorName IS NULL OR LOWER(a.attendance.doctorName) LIKE LOWER(CONCAT('%', :doctorName, '%')))")
    List<Appointment> searchByPatient(
            @org.springframework.data.repository.query.Param("patientId") UUID patientId,
            @org.springframework.data.repository.query.Param("specialty") String specialty,
            @org.springframework.data.repository.query.Param("doctorName") String doctorName
    );
}
