import { Component, OnInit } from '@angular/core';
import { AppointmentService } from '../../../Services/appointment.service';
import { AppointmentDTO } from '../../../DTOs/appointment/appointment.DTO';
import { UserService } from '../../../Services/user.service';

@Component({
  selector: 'app-user-manage-appointment',
  templateUrl: './user-manage-appointment.component.html',
  styleUrl: './user-manage-appointment.component.css'
})
export class UserManageAppointmentComponent implements OnInit{
  appointmentScheduled: { items: AppointmentDTO[]; totalPages: number; currentPage: number } | null = null;
  editingCompleted: { [apptId: number]: boolean } = {};
  userId!: number;
  limit: number = 10;

  constructor(private appointmentService: AppointmentService,
              private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userId = this.userService.getUserFromCookie()!.id;
    this.loadAppointment();
  }

  loadAppointment(page: number = 0) {
    this.appointmentService.getAppointmentByUser(this.userId, page, this.limit)
      .subscribe(resp => {
        this.appointmentScheduled = {
          items: resp.objects,
          totalPages: resp.total,
          currentPage: page
        };
      });
  }

  prevCompleted(): void {
    if (this.appointmentScheduled!.currentPage > 0) 
      this.loadAppointment(this.appointmentScheduled!.currentPage - 1);
  }
  nextCompleted(): void {
    if (this.appointmentScheduled!.currentPage + 1 < this.appointmentScheduled!.totalPages)
      this.loadAppointment(this.appointmentScheduled!.currentPage + 1);
  }

  toggleEditCompleted(id: number): void {
    this.editingCompleted[id] = !this.editingCompleted[id];
  }

  trackByDate(_i: number, d: string)    { return d; }

  trackById(_i: number, appt: AppointmentDTO) { return appt.id; }
}
