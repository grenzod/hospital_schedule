import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DepartmentResponse } from '../../Models/departmentResponse';
import { DepartmentService } from '../../Services/department.service';

@Component({
    selector: 'app-book',
    templateUrl: './book.component.html',
    styleUrls: ['./book.component.css']
})
export class BookComponent implements OnInit {
    departments: DepartmentResponse[] = [];
    currentPage = 0;
    totalPages = 0;
    searchKeyword = '';
    pageSize = 10;

    constructor(
        private router: Router,
        private departmentService: DepartmentService
    ) { }

    ngOnInit(): void {
        this.loadDepartments();
    }

    loadDepartments(page: number = 0): void {
        this.departmentService.getDepartments(this.searchKeyword, page, this.pageSize)
            .subscribe({
                next: (response) => {
                    this.departments = response.objects;
                    this.totalPages = response.total;
                    this.currentPage = page;
                },
                error: (error) => {
                    console.error('Error loading departments:', error);
                }
            });
    }

    selectDepartment(id: number): void {
        this.router.navigate(['/professional', id]);
    }

    onSearch(): void {
        this.currentPage = 0;
        this.loadDepartments();
    }

    onPageChange(page: number): void {
        this.loadDepartments(page);
    }

    handleImageError(event: any): void {
      event.target.src = 'assets/images/error-404.webp'; 
  }
}