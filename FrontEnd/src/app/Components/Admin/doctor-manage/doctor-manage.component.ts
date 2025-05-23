import { Component, OnInit } from '@angular/core';
import { DoctorService } from '../../../Services/doctor.service';
import { DoctorResponse } from '../../../Models/doctorResponse';
import { DepartmentService } from '../../../Services/department.service';
import { DepartmentResponse } from '../../../Models/departmentResponse';

@Component({
  selector: 'app-doctor-manage',
  templateUrl: './doctor-manage.component.html',
  styleUrl: './doctor-manage.component.css'
})
export class DoctorManageComponent implements OnInit {
  keyword: string = '';
  doctorList: { items: DoctorResponse[]; totalPages: number; currentPage: number; } | null = null;
  limit: number = 10;
  departmentList: DepartmentResponse[] = [];
  departmentSelected: number | null = null;
  statusOptions: boolean[] = [true, false];
  statusSelected: boolean | null = null;

  constructor(private doctorService: DoctorService,
              private departmentService: DepartmentService
  ) { }

  ngOnInit(): void {
    this.getAllDoctorsByAdmin();
    this.loadDepartment();
  }

  getAllDoctorsByAdmin(page: number = 0) {
    this.doctorService.getAllDoctors(this.keyword, this.departmentSelected, this.statusSelected, page, this.limit).subscribe({
      next: (response: any) => {
        this.doctorList = {
          items: response.objects,
          totalPages: response.total,
          currentPage: page
        }
      },
      error: (err: any) => alert(err.error)
    });
  }

  prevCompleted(): void {
    if (this.doctorList!.currentPage > 0)
      this.getAllDoctorsByAdmin(this.doctorList!.currentPage - 1);
  }
  nextCompleted(): void {
    if (this.doctorList!.currentPage + 1 < this.doctorList!.totalPages)
      this.getAllDoctorsByAdmin(this.doctorList!.currentPage + 1);
  }

  trackByDate(_i: number, d: string) { return d; }

  trackById(_i: number, appt: DoctorResponse) { return appt.id; }

  toggleStatusDoctor(id: number) {
    this.doctorService.toggleStatusDoctor(id).subscribe({
      next:(response: any) => {
        alert("Thành công: " + response);
        this.getAllDoctorsByAdmin();
      },
      error:(err: any) => alert("Có lỗi : " + err.error)
    });
  }

  handleImageError(event: any): void {
    event.target.src = 'assets/images/error-404.webp'; 
  }

  loadDepartment() {
    this.departmentService.getDepartments()
      .subscribe({
        next: (response) => {
          this.departmentList = response.objects;
        },
        error: (error) => {
          console.error('Error loading departments: ', error.error);
        }
      });
  }

}
