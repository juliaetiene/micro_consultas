package br.com.susUpa.consultas.repository;

import br.com.susUpa.consultas.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Attendance a WHERE " +
            "(:specialty IS NULL OR LOWER(a.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))) AND " +
            "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%')))")
    java.util.List<Attendance> searchBySpecialtyAndCity(
            @org.springframework.data.repository.query.Param("specialty") String specialty,
            @org.springframework.data.repository.query.Param("city") String city
    );
}
