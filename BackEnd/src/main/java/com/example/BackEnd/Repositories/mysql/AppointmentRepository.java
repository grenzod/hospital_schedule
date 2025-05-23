package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.DTO.ObjectStatsDTO;
import com.example.BackEnd.Entity.Appointment;
import com.example.BackEnd.Enum.AppointmentStatus;
import com.example.BackEnd.Enum.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    int countByAppointmentDate(LocalDate appointmentDate);

    int countByAppointmentDateAndAppointmentTime(LocalDate appointmentDate, LocalTime appointmentTime);

    boolean existsByPatient_IdAndAppointmentStatus(Integer patientId, AppointmentStatus appointmentStatus);

    @Query("""
        SELECT a FROM Appointment a
        WHERE (:namePatient IS NULL OR a.patient.fullName LIKE %:namePatient%)
          AND (:nameDoctor IS NULL OR a.doctor.user.fullName LIKE %:nameDoctor%)
          AND (:appointmentStatus IS NULL OR a.appointmentStatus = :appointmentStatus)
          AND (:paymentStatus IS NULL OR a.paymentStatus = :paymentStatus)
        """)
    Page<Appointment> findByAdmin(
        @Param("namePatient")       String namePatient,
        @Param("nameDoctor")        String nameDoctor,
        @Param("appointmentStatus") AppointmentStatus appointmentStatus,
        @Param("paymentStatus")     PaymentStatus paymentStatus,
        Pageable pageable
    );

    Page<Appointment> findByPatient_Id(Integer patientId, Pageable pageable);

    Page<Appointment> findByAppointmentStatusAndPaymentStatusOrderByAppointmentTimeAsc(
        AppointmentStatus appointmentStatus,
        PaymentStatus paymentStatus,
        Pageable pageable
    );

    Page<Appointment> findByDoctor_IdAndAppointmentDateAndAppointmentStatusOrderByAppointmentTimeAsc(
        Integer doctorId,
        LocalDate appointmentDate,
        AppointmentStatus appointmentStatus,
        Pageable pageable
    );

    Page<Appointment> findByDoctor_IdAndAppointmentStatusOrderByAppointmentTimeAsc(
        Integer doctorId,
        AppointmentStatus appointmentStatus,
        Pageable pageable
    );

    @Query("""
                SELECT new com.example.BackEnd.DTO.ObjectStatsDTO(
                        YEAR(u.createdAt),
                        MONTH(u.createdAt),
                        COUNT(u)
                )
                FROM Appointment u
                        WHERE u.createdAt >= :startDate
                        GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
                        ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)
        """)
        List<ObjectStatsDTO> countNewAppointmentsPerMonthSince(@Param("startDate") LocalDateTime startDate);
}
