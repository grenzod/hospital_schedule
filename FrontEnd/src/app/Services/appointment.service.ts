import { Observable } from "rxjs";
import { HttpClient, HttpParams, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { PaymentStatus } from "../Enums/payment-status.enum";
import { AppointmentStatus } from "../Enums/appointment-status.enum";

export interface Appointment {
    userId: number | undefined;
    doctorId: number;
    appointmentDate: string;
    appointmentTime: string;
    full_name: string;
    phone_number: string;
    symptoms: string;
}

export interface AppointmentStats {
    year: number;
    month: number;
    count: number;
}

@Injectable({
    providedIn: 'root'
})
export class AppointmentService {
    private api = `${environment.apiBaseUrl}/appointments`;

    constructor(private http: HttpClient) { }

    scheduleAppointment(appointment: Appointment): Observable<any> {
        return this.http.post<any>(`${this.api}/schedule`, appointment);
    }

    getAppointmentsByAdmin(
        page: number, limit: number,
        namePatient: string, nameDoctor: string,
        appointmentStatus?: AppointmentStatus | null,
        paymentStatus?: PaymentStatus | null
    ): Observable<any> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('limit', limit.toString())
            .set('patient', namePatient)
            .set('doctor', nameDoctor);
        if (appointmentStatus) params = params.set('appointment', appointmentStatus);
        if (paymentStatus) params = params.set('payment', paymentStatus);
        return this.http.get<any>(this.api, { params });
    }

    getAnalyzeAppointments(months: number = 6): Observable<AppointmentStats[]> {
        const params = new HttpParams()
            .set("months", months.toString())
        return this.http.get<AppointmentStats[]>(`${this.api}/analyze`, { params });
    }

    getAppointmentByDoctorIdAndDate(userId: number, date: string, page: number = 0, limit: number = 5): Observable<any> {
        const params = new HttpParams()
            .set('date', date)
            .set('page', page.toString())
            .set('limit', limit.toString());
        return this.http.get<any>(`${this.api}/ByDoctor/Pending/${userId}`, { params });
    }

    getAllAppoitmentCompleteByDoctorId(userId: number, page: number = 0, limit: number = 5): Observable<any> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('limit', limit.toString());
        return this.http.get<any>(`${this.api}/ByDoctor/Complete/${userId}`, { params });
    }

    getAppointmentByUser(userId: number, page: number = 0, limit: number = 5): Observable<any> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('limit', limit.toString());
        return this.http.get<any>(`${this.api}/ByUser/${userId}`, { params });
    }

    upgradeStatusAppointment(id: number, treatment: string): Observable<any> {
        const formData = new FormData();
        formData.append('treatment', treatment);
        return this.http.put<any>(`${this.api}/${id}`, formData);
    }

    havePaid(id: number): Observable<any> {
        const params = new HttpParams().set('id', id.toString());
        return this.http.put<any>(`${this.api}/paid`, {}, { params });
    }

    generatePDF(id: number): Observable<Blob> {
        return this.http.get(`${this.api}/medical-report/${id}`, {
            responseType: 'blob',
            headers: new HttpHeaders({
                'Accept': 'application/pdf'
            })
        });
    }
}
