package com.example.BackEnd.Response;

import com.example.BackEnd.Entity.Appointment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentResponse {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("patient_name")
    private String patientName;

    @JsonProperty("doctor_name")
    private String doctorName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty
    private String symptoms;

    @JsonProperty("treatment_plan")
    private String treatmentPlan;

    @JsonProperty("appointment_status")
    private String status;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("appointment_date")
    private String appointmentDate;

    @JsonProperty("appointment_time")
    private String appointmentTime;

    public static AppointmentResponse fromAppointment(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .phoneNumber(appointment.getPhoneNumber())
                .symptoms(appointment.getSymptoms())
                .treatmentPlan(appointment.getTreatmentPlan())
                .status(appointment.getAppointmentStatus().toString())
                .paymentStatus(appointment.getPaymentStatus().toString())
                .appointmentDate(appointment.getAppointmentDate().toString())
                .appointmentTime(appointment.getAppointmentTime().toString())
                .paymentStatus(appointment.getPaymentStatus().toString())
                .build();
    }
}
