import {
  Component,
  ElementRef,
  OnInit,
  QueryList,
  ViewChildren,
  Renderer2
} from '@angular/core';
import { UserService } from '../../../Services/user.service';
import { DepartmentService } from '../../../Services/department.service';
import { DoctorService } from '../../../Services/doctor.service';
import { UserResponse } from '../../../Models/userResponse';
import { DepartmentResponse } from '../../../Models/departmentResponse';

@Component({
  selector: 'app-user-manage',
  templateUrl: './user-manage.component.html',
  styleUrls: ['./user-manage.component.css']
})
export class UserManageComponent implements OnInit {
  @ViewChildren('addDoctorForm') addDoctorForms!: QueryList<ElementRef<HTMLDivElement>>;
  @ViewChildren('showInforUser') showInforUsers!: QueryList<ElementRef<HTMLDivElement>>;

  keyword = '';
  currentPage = 0;
  limit = 10;
  totalPages = 1;

  users: UserResponse[] = [];
  departments: DepartmentResponse[] = [];

  newDoctorData = {
    departmentId: null as number | null,
    licenseNumber: '',
    file: null as File | null
  };

  activeAddDoctorUserId: number | null = null;
  activeShowInforUserId: number | null = null;

  constructor(
    private userService: UserService,
    private departmentService: DepartmentService,
    private doctorService: DoctorService,
    private renderer: Renderer2
  ) { }

  ngOnInit(): void {
    this.loadUsers();
    this.loadDepartments();
  }

  private loadUsers(): void {
    this.userService
      .getAllUsers(this.keyword, this.currentPage, this.limit)
      .subscribe(res => {
        this.users = res.objects;
        this.totalPages = Math.ceil(res.total / this.limit);
        this.toggleInlineForms("doctor");
        this.toggleInlineForms("user");
      });
  }

  private loadDepartments(): void {
    this.departmentService
      .getDepartments('', 0, 100)
      .subscribe({
        next: res => (this.departments = res.objects),
        error: err => console.error('Failed to load departments:', err)
      });
  }

  searchUsers(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  toggleUserStatus(id: number): void {
    this.userService.toggleUserStatus(id)
      .subscribe({
        next: () => {
          alert('User status updated successfully');
          this.loadUsers();
        },
        error: err => alert('Error updating user status: ' + err.error?.message || 'Unknown error')
      });
  }

  onAddDoctorClick(userId: number): void {
    this.activeAddDoctorUserId = this.activeAddDoctorUserId === userId ? null : userId;
    if (this.activeAddDoctorUserId === userId) {
      this.newDoctorData = { departmentId: null, licenseNumber: '', file: null };
    }
    this.toggleInlineForms("doctor");
  }

  onShowInforUserClick(userId: number): void {
    this.activeShowInforUserId = this.activeShowInforUserId === userId ? null : userId;
    this.toggleInlineForms("user");
  }

  private toggleInlineForms(type: "doctor" | "user"): void {
    const targetList = type === "doctor" 
      ? this.addDoctorForms 
      : this.showInforUsers;

    targetList?.forEach((elementRef, index) => {
      const user = this.users[index];
      const shouldShow = user && (
        type === "doctor" 
          ? user.id === this.activeAddDoctorUserId 
          : user.id === this.activeShowInforUserId
      );
      
      this.renderer[shouldShow ? 'removeClass' : 'addClass'](
        elementRef.nativeElement, 
        'hidden'
      );
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.newDoctorData.file = input.files?.[0] || null;
  }

  submitAddDoctor(userId: number): void {
    const { departmentId, licenseNumber, file } = this.newDoctorData;
    
    if (!departmentId || !licenseNumber || !file) {
      alert('Please fill all required fields');
      return;
    }
    
    if (isNaN(Number(licenseNumber))) {
      alert('License number must be a valid number');
      return;
    }

    this.doctorService.addDoctor(
      userId, 
      departmentId,
      Number(licenseNumber),
      file
    ).subscribe({
      next: () => {
        alert('Doctor added successfully');
        this.activeAddDoctorUserId = null;
        this.loadUsers();
      },
      error: err => {
        alert(`Error: ${err.error}`);
      }
    });
  }

  handleImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/default-avatar.webp';
  }
}