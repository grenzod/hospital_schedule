package com.example.BackEnd.Service.impl;

import com.example.BackEnd.DTO.AppointmentDTO;
import com.example.BackEnd.Entity.Appointment;
import com.example.BackEnd.Entity.Doctor;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Enum.AppointmentStatus;
import com.example.BackEnd.Enum.PaymentStatus;
import com.example.BackEnd.Repositories.mysql.AppointmentRepository;
import com.example.BackEnd.Repositories.mysql.DoctorRepository;
import com.example.BackEnd.Repositories.mysql.UserRepository;
import com.example.BackEnd.Response.AppointmentResponse;
import com.example.BackEnd.Service.IAppointmentService;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
public class AppointmentService implements IAppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Page<AppointmentResponse> getAllAppointment(
            String namePatient,
            String nameDoctor,
            AppointmentStatus appointmentStatus,
            PaymentStatus paymentStatus,
            PageRequest pageRequest) {
        Page<Appointment> appointmentPage = appointmentRepository
                .findByAdmin(namePatient, nameDoctor, appointmentStatus, paymentStatus, pageRequest);
        return appointmentPage.map(AppointmentResponse::fromAppointment);
    }

    @Override
    public Page<AppointmentResponse> findAppointmentsByUser(PageRequest pageRequest, Integer id) {
        Page<Appointment> appointmentPage = appointmentRepository
                .findByPatient_Id(id, pageRequest);
        return appointmentPage.map(AppointmentResponse::fromAppointment);
    }

    @Override
    public Page<AppointmentResponse> findAllAppointmentPendingByDoctor(PageRequest pageRequest, Integer id,
            LocalDate date) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer doctorId = doctorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Doctor not found")).getId();
        Page<Appointment> appointmentPage = appointmentRepository
                .findByDoctor_IdAndAppointmentDateAndAppointmentStatusOrderByAppointmentTimeAsc(
                        doctorId, date, AppointmentStatus.PENDING, pageRequest);
        return appointmentPage.map(AppointmentResponse::fromAppointment);
    }

    @Override
    public Page<AppointmentResponse> findAllAppointmentCompleteByDoctor(PageRequest pageRequest, Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer doctorId = doctorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Doctor not found")).getId();
        Page<Appointment> appointmentPage = appointmentRepository
                .findByDoctor_IdAndAppointmentStatusOrderByAppointmentTimeAsc(
                        doctorId, AppointmentStatus.COMPLETED, pageRequest);
        return appointmentPage.map(AppointmentResponse::fromAppointment);
    }

    @Override
    public synchronized Appointment scheduleAnAppointment(AppointmentDTO appointmentDTO) throws Exception {
        Integer userId = appointmentDTO.getUserId();
        Integer doctorId = appointmentDTO.getDoctorId();
        LocalDate appointmentDate = appointmentDTO.getAppointmentDate();
        LocalTime appointmentTime = appointmentDTO.getAppointmentTime();
        String phoneNumber = appointmentDTO.getPhoneNumber();
        String fullName = appointmentDTO.getFullName();
        if (appointmentRepository.existsByPatient_IdAndAppointmentStatus(userId, AppointmentStatus.PENDING)) {
            throw new Exception("You have taken An appointment, You only take new after that are completelly !!");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

        int appointmentCount = appointmentRepository.countByAppointmentDateAndAppointmentTime(appointmentDate,
                appointmentTime);
        if (appointmentCount >= 10) {
            throw new Exception("This time has too many appointments, please choose another time.");
        }

        Appointment appointment = Appointment.builder()
                .patient(user)
                .doctor(doctor)
                .appointmentStatus(AppointmentStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .phoneNumber(phoneNumber)
                .fullName(fullName)
                .symptoms(appointmentDTO.getSymptoms())
                .treatmentPlan("")
                .build();
        return appointmentRepository.save(appointment);
    }

    public LocalDate selectAvailableAppointmentDate() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate saturday = today.with(DayOfWeek.SATURDAY);

        for (LocalDate date = today; date.isBefore(saturday); date = date.plusDays(1)) {
            int appointmentCount = appointmentRepository.countByAppointmentDate(date);
            if (appointmentCount <= 30) {
                return date;
            }
        }
        throw new Exception("Dates are full, wait for next week");
    }

    @Override
    public Appointment upgradeAppointment(Integer id, String treatment) {
        Appointment appointment = appointmentRepository
                .findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setTreatmentPlan(treatment);
        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    @Override
    public byte[] generateReport(Integer id) {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/times.ttf", PdfEncodings.IDENTITY_H);
            
            Style titleStyle = new Style()
                .setFont(font)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

            Style headerStyle = new Style()
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);

            Style contentStyle = new Style()
                .setFont(font)
                .setFontSize(12)
                .setMarginLeft(20);

            document.add(new Paragraph("MEDICAL REPORT")
                .addStyle(titleStyle)
                .setMarginBottom(30));

            document.add(new LineSeparator(new SolidLine(1f))
                .setMarginBottom(20));

            document.add(new Paragraph("PATIENT INFORMATION").addStyle(headerStyle));
            document.add(new Paragraph("Full Name: " + appointment.getFullName()).addStyle(contentStyle));
            document.add(new Paragraph("Phone Number: " + appointment.getPhoneNumber()).addStyle(contentStyle));

            document.add(new Paragraph("APPOINTMENT DETAILS").addStyle(headerStyle));
            document.add(new Paragraph("Doctor: " + appointment.getDoctor().getUser().getFullName()).addStyle(contentStyle));
            document.add(new Paragraph("Date: " + appointment.getAppointmentDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).addStyle(contentStyle));
            document.add(new Paragraph("Time: " + appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"))).addStyle(contentStyle));

            document.add(new Paragraph("MEDICAL INFORMATION").addStyle(headerStyle));
            document.add(new Paragraph("Symptoms:").addStyle(contentStyle));
            document.add(new Paragraph(appointment.getSymptoms())
                .addStyle(contentStyle)
                .setMarginLeft(40));

            document.add(new Paragraph("Treatment Plan:").addStyle(contentStyle));
            document.add(new Paragraph(appointment.getTreatmentPlan())
                .addStyle(contentStyle)
                .setMarginLeft(40));

            document.add(new LineSeparator(new SolidLine(1f))
                .setMarginTop(30));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10)
                .setMarginTop(10));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

}
