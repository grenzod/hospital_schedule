import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DoctorService } from '../../Services/doctor.service';
import { DoctorResponse } from '../../Models/doctorResponse';
import { DepartmentService } from '../../Services/department.service';
import { DepartmentResponse } from '../../Models/departmentResponse';

@Component({
  selector: 'app-doctor-list',
  templateUrl: './doctor-list.component.html',
  styleUrl: './doctor-list.component.css'
})
export class DoctorListComponent implements OnInit {
  doctors: DoctorResponse[] = [];
  departmentList: DepartmentResponse[] = [];
  keyword: string = '';
  currentPage: number = 0;
  limit: number = 10;
  totalPages: number = 1;
  departmentId: number | null = null;
  departmentSelected: number | null = null;

  constructor(
    private activeRoute: ActivatedRoute,
    private doctorService: DoctorService,
    private departmentService: DepartmentService 
  ) {}

  ngOnInit(): void {
    this.loadDepartment(); 
    this.activeRoute.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id != '0') {
        this.departmentId = Number(id);
        this.loadDoctorsByDepartment();
      } else {
        this.loadDoctors();
      }
    });
  }

  loadDoctors(): void {
    this.doctorService.getAllDoctors(this.keyword,  this.departmentSelected, true , this.currentPage, this.limit)
      .subscribe(response => {
        this.doctors = response.objects;
        this.totalPages = response.total;
      });
  }

  loadDoctorsByDepartment(): void {
    if (this.departmentId !== null) {
      this.doctorService.getDoctorByDepartmentId(this.departmentId, this.currentPage, this.limit)
        .subscribe(response => {
          this.doctors = response.objects;
          this.totalPages = response.total;
        });
    }
  }

  searchDoctors(): void {
    this.currentPage = 0;
    this.loadDoctors();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadDoctors();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadDoctors();
    }
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
