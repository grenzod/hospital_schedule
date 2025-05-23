import {
    IsString,
    IsEnum,
    IsDateString
} from 'class-validator';
import { PaymentStatus } from '../../Enums/payment-status.enum';
import { AppointmentStatus } from '../../Enums/appointment-status.enum';

export class AppointmentDTO {
    id: number;

    @IsString()
    patient_name: string;

    @IsString()
    doctor_name: string;

    @IsString()
    symptoms: string;

    @IsString()
    treatment_plan: string;

    @IsString()
    phone_number: string;

    @IsDateString()
    appointment_date: string;

    @IsDateString()
    appointment_time: string;

    @IsEnum(AppointmentStatus)
    appointment_status: AppointmentStatus;

    @IsEnum(PaymentStatus)
    payment_status: PaymentStatus;

    constructor(data: any) {
        this.id = data.id;
        this.patient_name = data.patient_name;
        this.doctor_name = data.doctor_name;
        this.symptoms = data.symptoms;
        this.treatment_plan = data.treatment_plan;
        this.phone_number = data.phone_number;
        this.appointment_date = data.appointment_date;
        this.appointment_time = data.appointment_time;
        this.appointment_status = data.appointment_status;
        this.payment_status = data.payment_status;
    }
}