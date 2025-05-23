package com.example.BackEnd.Controller;

import com.example.BackEnd.DTO.AppointmentDTO;
import com.example.BackEnd.DTO.ObjectStatsDTO;
import com.example.BackEnd.Entity.Appointment;
import com.example.BackEnd.Enum.AppointmentStatus;
import com.example.BackEnd.Enum.PaymentStatus;
import com.example.BackEnd.Repositories.mysql.AppointmentRepository;
import com.example.BackEnd.Response.AppointmentResponse;
import com.example.BackEnd.Response.ObjectListResponse;
import com.example.BackEnd.Service.IAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/appointments")
@RequiredArgsConstructor
public class AppointmentController {
        private final IAppointmentService iAppointmentService;
        private final AppointmentRepository appointmentRepository;

        @GetMapping("")
        public ResponseEntity<?> getAllAppointment(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(name = "patient", defaultValue = "") String namePatient,
                        @RequestParam(name = "doctor", defaultValue = "") String nameDoctor,
                        @RequestParam(name = "appointment", required = false) AppointmentStatus appointmentStatus,
                        @RequestParam(name = "payment", required = false) PaymentStatus paymentStatus) {
                PageRequest pageRequest = PageRequest.of(
                                page,
                                limit,
                                Sort.by("id").descending());
                Page<AppointmentResponse> appointments = iAppointmentService.getAllAppointment(
                                namePatient,
                                nameDoctor,
                                appointmentStatus,
                                paymentStatus,
                                pageRequest);
                int totalPages = appointments.getTotalPages();
                List<AppointmentResponse> appointmentResponseList = appointments.getContent();

                return ResponseEntity.ok().body(ObjectListResponse.builder()
                                .objects(appointmentResponseList)
                                .total(totalPages)
                                .build());
        }

        @GetMapping("/analyze")
        public List<ObjectStatsDTO> newAppointmentsLastMonths(
                        @RequestParam(defaultValue = "6") int months) {
                LocalDateTime start = LocalDateTime.now()
                                .minusMonths(months - 1)
                                .withDayOfMonth(1)
                                .toLocalDate().atStartOfDay();
                return appointmentRepository.countNewAppointmentsPerMonthSince(start);
        }

        @GetMapping("/ByUser/{id}")
        public ResponseEntity<?> getAppointmentsByUserId(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int limit,
                        @PathVariable("id") Integer id) {
                PageRequest pageRequest = PageRequest.of(
                                page,
                                limit,
                                Sort.by("createdAt").descending());
                Page<AppointmentResponse> appointments = iAppointmentService.findAppointmentsByUser(pageRequest, id);
                int totalPages = appointments.getTotalPages();
                List<AppointmentResponse> appointmentResponseList = appointments.getContent();

                return ResponseEntity.ok().body(ObjectListResponse.builder()
                                .objects(appointmentResponseList)
                                .total(totalPages)
                                .build());
        }

        @GetMapping("/ByDoctor/Pending/{id}")
        public ResponseEntity<?> getAllAppointmentsPendingByDoctorId(
                        @PathVariable("id") Integer id,
                        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date,
                        @RequestParam(defaultValue = "5") int limit,
                        @RequestParam(defaultValue = "0") int page) {
                try {
                        PageRequest pageRequest = PageRequest.of(
                                        page,
                                        limit,
                                        Sort.by("appointmentTime").descending());
                        Page<AppointmentResponse> appointments = iAppointmentService.findAllAppointmentPendingByDoctor(
                                        pageRequest,
                                        id,
                                        date);
                        int totalPages = appointments.getTotalPages();
                        List<AppointmentResponse> appointmentResponseList = appointments.getContent();

                        return ResponseEntity.ok().body(ObjectListResponse.builder()
                                        .objects(appointmentResponseList)
                                        .total(totalPages)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Have error");
                }
        }

        @GetMapping("/ByDoctor/Complete/{id}")
        public ResponseEntity<?> getAllAppointmentsCompleteByDoctorId(
                        @PathVariable("id") Integer id,
                        @RequestParam(defaultValue = "5") int limit,
                        @RequestParam(defaultValue = "0") int page) {
                try {
                        PageRequest pageRequest = PageRequest.of(
                                        page,
                                        limit,
                                        Sort.by("appointmentTime").descending());
                        Page<AppointmentResponse> appointments = iAppointmentService.findAllAppointmentCompleteByDoctor(
                                        pageRequest,
                                        id);
                        int totalPages = appointments.getTotalPages();
                        List<AppointmentResponse> appointmentResponseList = appointments.getContent();

                        return ResponseEntity.ok().body(ObjectListResponse.builder()
                                        .objects(appointmentResponseList)
                                        .total(totalPages)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Have error");
                }
        }

        @PutMapping("/{id}")
        public ResponseEntity<?> upgradeStatusAppointment(
                        @PathVariable(name = "id") Integer id,
                        @RequestPart("treatment") String treatment) {
                try {
                        Appointment appointment = iAppointmentService.upgradeAppointment(id, treatment);
                        return ResponseEntity.ok().body(appointment);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Can't upgrade the appointment");
                }
        }

        @PutMapping("/paid")
        public ResponseEntity<?> havePaid(@RequestParam("id") Integer id) {
                try {
                        Appointment appt = appointmentRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy appointment"));
                        appt.setPaymentStatus(PaymentStatus.PAID);
                        return ResponseEntity.ok(appointmentRepository.save(appt));
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Có lỗi khi cập nhật thanh toán");
                }
        }

        @PostMapping("/schedule")
        public ResponseEntity<?> makeAnAppointment(
                        @RequestBody AppointmentDTO appointmentDTO) {
                try {
                        Appointment appointment = iAppointmentService.scheduleAnAppointment(appointmentDTO);
                        return ResponseEntity.ok().body(appointment);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                }
        }

        @GetMapping("/medical-report/{id}")
        public ResponseEntity<byte[]> generateMedicalReport(@PathVariable Integer id) {
                byte[] pdfBytes = iAppointmentService.generateReport(id);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(ContentDisposition.builder("attachment")
                                .filename("medical_report_" + id + ".pdf")
                                .build());
                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }
}
