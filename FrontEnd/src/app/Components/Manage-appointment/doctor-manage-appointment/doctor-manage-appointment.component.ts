import { Component, OnInit } from '@angular/core';
import { AppointmentService } from '../../../Services/appointment.service';
import { UserService } from '../../../Services/user.service';
import { AppointmentDTO } from '../../../DTOs/appointment/appointment.DTO';

@Component({
  selector: 'app-doctor-manage-appointment',
  templateUrl: './doctor-manage-appointment.component.html',
  styleUrls: ['./doctor-manage-appointment.component.css']
})
export class DoctorManageAppointmentComponent implements OnInit {
  dates: string[] = [];
  schedule = new Map<string, { items: AppointmentDTO[]; totalPages: number; currentPage: number }>();
  expandedDates = new Set<string>();
  completedSchedule: { items: AppointmentDTO[]; totalPages: number; currentPage: number } | null = null;

  doctorId!: number;
  limit = 5;

  // Pending edit map
  editingPending: { [apptId: number]: boolean } = {};
  diagnosisMap:    { [apptId: number]: string } = {};

  // Completed detail map
  editingCompleted: { [apptId: number]: boolean } = {};

  constructor(
    private appointmentService: AppointmentService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.doctorId = this.userService.getUserFromCookie()!.id;
    this.dates = this.computeWeekDates();
    this.loadCompleted();
  }

  private computeWeekDates(): string[] {
    const today = new Date(), d0 = today.getDay() || 7;
    return Array.from({ length: 7 - (d0 - 1) }, (_, i) => {
      const dt = new Date(today);
      dt.setDate(today.getDate() + i);
      return dt.toISOString().substr(0, 10);
    });
  }

  loadDate(date: string, page = 0): void {
    if (this.expandedDates.has(date)) {
      this.expandedDates.delete(date);
      return;
    }
    this.appointmentService.getAppointmentByDoctorIdAndDate(this.doctorId, date, page, this.limit)
      .subscribe(resp => {
        this.schedule.set(date, {
          items: resp.objects,
          totalPages: resp.total,
          currentPage: page
        });
        this.expandedDates.add(date);
      });
  }

  prevPage(date: string): void {
    const info = this.schedule.get(date)!;
    if (info.currentPage > 0) this.loadDate(date, info.currentPage - 1);
  }
  nextPage(date: string): void {
    const info = this.schedule.get(date)!;
    if (info.currentPage + 1 < info.totalPages) this.loadDate(date, info.currentPage + 1);
  }

  toggleEditPending(id: number): void {
    this.editingPending[id] = !this.editingPending[id];
    if (this.editingPending[id] && !this.diagnosisMap[id]) this.diagnosisMap[id] = '';
  }

  saveDiagnosis(date: string, id: number): void {
    const treatment = this.diagnosisMap[id] || '';
    this.appointmentService.upgradeStatusAppointment(id, treatment).subscribe(() => {
      this.toggleEditPending(id);
      const page = this.schedule.get(date)!.currentPage;
      this.loadDate(date, page);
      this.loadCompleted();
    });
  }

  private loadCompleted(page = 0): void {
    this.appointmentService.getAllAppoitmentCompleteByDoctorId(this.doctorId, page, this.limit)
      .subscribe(resp => {
        this.completedSchedule = {
          items: resp.objects,
          totalPages: resp.total,
          currentPage: page
        };
      });
  }

  prevCompleted(): void {
    if (this.completedSchedule!.currentPage > 0) this.loadCompleted(this.completedSchedule!.currentPage - 1);
  }
  nextCompleted(): void {
    if (this.completedSchedule!.currentPage + 1 < this.completedSchedule!.totalPages)
      this.loadCompleted(this.completedSchedule!.currentPage + 1);
  }

  toggleEditCompleted(id: number): void {
    this.editingCompleted[id] = !this.editingCompleted[id];
  }

  trackByDate(_i: number, d: string)    { return d; }
  trackById(_i: number, appt: AppointmentDTO) { return appt.id; }
}
