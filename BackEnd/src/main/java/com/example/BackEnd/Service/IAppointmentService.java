package com.example.BackEnd.Service;

import com.example.BackEnd.DTO.AppointmentDTO;
import com.example.BackEnd.Entity.Appointment;
import com.example.BackEnd.Enum.AppointmentStatus;
import com.example.BackEnd.Enum.PaymentStatus;
import com.example.BackEnd.Response.AppointmentResponse;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IAppointmentService {
    Page<AppointmentResponse> getAllAppointment(
        String namePatient,
        String nameDoctor,
        AppointmentStatus appointmentStatus,
        PaymentStatus paymentStatus,
        PageRequest pageRequest
    );
    Appointment scheduleAnAppointment(AppointmentDTO appointmentDTO) throws Exception;
    Appointment upgradeAppointment(Integer id, String treatment);
    Page<AppointmentResponse> findAppointmentsByUser(PageRequest pageRequest, Integer id);
    Page<AppointmentResponse> findAllAppointmentPendingByDoctor(PageRequest pageRequest, Integer id, LocalDate date);
    Page<AppointmentResponse> findAllAppointmentCompleteByDoctor(PageRequest pageRequest, Integer id);
    byte[] generateReport(Integer id);
}
