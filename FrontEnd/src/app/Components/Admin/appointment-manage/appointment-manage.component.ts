import { Component, OnInit } from '@angular/core';
import { PaymentStatus } from '../../../Enums/payment-status.enum';
import { AppointmentStatus } from '../../../Enums/appointment-status.enum';
import { AppointmentDTO } from '../../../DTOs/appointment/appointment.DTO';
import { AppointmentService } from '../../../Services/appointment.service';
import { environment } from '../../../Environments/environment';
import { TokenService } from '../../../Services/token.service';

@Component({
  selector: 'app-appointment-manage',
  templateUrl: './appointment-manage.component.html',
  styleUrls: ['./appointment-manage.component.css']
})
export class AppointmentManageComponent implements OnInit {
  namePatient: string = "";
  nameDoctor: string = "";
  paymenStatus: PaymentStatus | null = null;
  appointmentStatus: AppointmentStatus | null = null;

  paymentStatusOptions = Object.values(PaymentStatus);
  appointmentStatusOptions = Object.values(AppointmentStatus);

  appointmentScheduled: { items: AppointmentDTO[]; totalPages: number; currentPage: number } | null = null;
  limit: number = 10;
  private api = `${environment.apiBaseUrl}/appointments`;

  constructor(private appointmentService: AppointmentService, private tokenService: TokenService) { }

  ngOnInit(): void {
    this.GetAppointmentByAdmin();
  }

  GetAppointmentByAdmin(page: number = 0): void {
    this.appointmentService.getAppointmentsByAdmin(
      page,
      this.limit,
      this.namePatient,
      this.nameDoctor,
      this.appointmentStatus,
      this.paymenStatus
    ).subscribe(resp => {
      this.appointmentScheduled = {
        items: resp.objects,
        totalPages: resp.total,
        currentPage: page
      };
    });
  }

  prevCompleted(): void {
    if (this.appointmentScheduled && this.appointmentScheduled.currentPage > 0) {
      this.GetAppointmentByAdmin(this.appointmentScheduled.currentPage - 1);
    }
  }

  nextCompleted(): void {
    if (this.appointmentScheduled &&
      this.appointmentScheduled.currentPage + 1 < this.appointmentScheduled.totalPages) {
      this.GetAppointmentByAdmin(this.appointmentScheduled.currentPage + 1);
    }
  }

  trackById(_index: number, appt: AppointmentDTO): number {
    return appt.id;
  }

  togglePaymentStatus(id: number): void {
    this.appointmentService.havePaid(id).subscribe({
      next: () => {
        alert("Cập nhật thành công");
        this.GetAppointmentByAdmin(this.appointmentScheduled!.currentPage);
      },
      error: err => alert(err.error)
    });
  }

  openPDF(id: number): void {
    this.appointmentService.generatePDF(id).subscribe(
      (pdfBlob: Blob) => {
        const blob = new Blob([pdfBlob], { type: 'application/pdf' });
        const blobUrl = window.URL.createObjectURL(blob);
        const newWindow = window.open(blobUrl, '_blank');
        if (!newWindow) {
          alert('Trình duyệt đã chặn pop-up. Vui lòng cho phép pop-up để xem tệp.');
        }
        setTimeout(() => window.URL.revokeObjectURL(blobUrl), 30000);
      },
      error => {
        console.error('Error loading PDF:', error);
        alert('Không thể tải PDF. Vui lòng thử lại.');
      }
    );
  }
}
