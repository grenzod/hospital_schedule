import { Component, OnInit } from '@angular/core';
import { ChartOptions, ChartType } from 'chart.js';
import { UserService, UserStats } from '../../../Services/user.service';
import { AppointmentService, AppointmentStats } from '../../../Services/appointment.service';
import { CommentService, CommentStats } from '../../../Services/comment.service';
import { ImageService } from '../../../Services/image.service';
import { DepartmentResponse } from '../../../Models/departmentResponse';
import { DepartmentService } from '../../../Services/department.service';

interface Advertisement {
  name: string;
  url: string;
}

interface Chart {
  data: number[];
  label: string;
  backgroundColor: string;
  borderColor: string;
  hoverBackgroundColor: string
}

@Component({
  selector: 'app-admin-dashboad',
  templateUrl: './admin-dashboad.component.html'
})
export class AdminDashboadComponent implements OnInit {
  public barChartType: ChartType = 'bar';
  public barChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true
      }
    },
    plugins: {
      legend: {
        display: true
      }
    },
    datasets: {
      bar: {
        barThickness: 40,        // Định chiều rộng cố định cho cột (pixels)
        maxBarThickness: 60,     // Chiều rộng tối đa của cột (pixels)
        barPercentage: 0.5,      // Phần trăm chiều rộng so với không gian có sẵn (0-1)
        categoryPercentage: 0.4   // Phần trăm không gian giữa các nhóm cột (0-1)
      }
    }
  };

  public barChartLabels: string[] = [];
  public barChartData: Chart[] = [
    {
      data: [],
      label: 'Người dùng mới',
      backgroundColor: 'rgba(54, 162, 235, 0.5)',
      borderColor: 'rgb(54, 162, 235)',
      hoverBackgroundColor: 'rgba(54, 162, 235, 0.7)'
    }
  ];

  public barChartLabelsOfAppointment: string[] = [];
  public barChartDataOfAppointment: Chart[] = [
    {
      data: [],
      label: 'Lịch đặt mới',
      backgroundColor: 'rgba(75, 192, 192, 0.5)',
      borderColor: 'rgb(75, 192, 192)',
      hoverBackgroundColor: 'rgba(75, 192, 192, 0.7)'
    }
  ];

  public barChartLabelsOfComment: string[] = [];
  public barChartDataOfComment: Chart[] = [
    {
      data: [],
      label: 'Bình luận mới',
      backgroundColor: 'rgba(153, 102, 255, 0.5)',
      borderColor: 'rgb(153, 102, 255)',
      hoverBackgroundColor: 'rgba(153, 102, 255, 0.7)'
    }
  ];
  slides: Advertisement[] = [];
  currentSlideIndex: number = 0;
  departments: { items: DepartmentResponse[]; totalPages: number; currentPage: number } | null = null;
  searchKeyword: string = '';
  limit: number = 5;
  image: File | null = null;
  imagePreview: string | null = null; 
  name: string = '';
  description: string = '';

  constructor(private userService: UserService,
    private appointmentService: AppointmentService,
    private commentService: CommentService,
    private imageService: ImageService,
    private departmentService: DepartmentService
  ) { }

  ngOnInit(): void {
    this.getInforNewUsers();
    this.getInforNewAppointments();
    this.getInforNewComments();
    this.loadSlides();
    this.loadDepartments();
  }

  getInforNewUsers(): void {
    this.userService.getAnalyzeUsers(6).subscribe((stats: UserStats[]) => {
      stats.sort((a, b) => a.year - b.year || a.month - b.month);
      this.barChartLabels = stats.map(s => `${s.month} / ${s.year}`);
      this.barChartData[0].data = stats.map(s => s.count);
    });
  }

  getInforNewAppointments(): void {
    this.appointmentService.getAnalyzeAppointments(6).subscribe((stats: AppointmentStats[]) => {
      stats.sort((a, b) => a.year - b.year || a.month - b.month);
      this.barChartLabelsOfAppointment = stats.map(s => `${s.month} / ${s.year}`);
      this.barChartDataOfAppointment[0].data = stats.map(s => s.count);
    });
  }

  getInforNewComments(): void {
    this.commentService.getAnalyzeComments(6).subscribe((stats: CommentStats[]) => {
      stats.sort((a, b) => a.year - b.year || a.month - b.month);
      this.barChartLabelsOfComment = stats.map(s => `${s.month} / ${s.year}`);
      this.barChartDataOfComment[0].data = stats.map(s => s.count);
    });
  }

  nextSlide() {
    this.currentSlideIndex = (this.currentSlideIndex + 1) % this.slides.length;
  }

  prevSlide() {
    this.currentSlideIndex = this.currentSlideIndex === 0
      ? this.slides.length - 1
      : this.currentSlideIndex - 1;
  }

  deleteCurrentSlide() {
    if (this.slides.length > 0) {
      const currentSlide = this.slides[this.currentSlideIndex];

      this.imageService.deleteImageByName(currentSlide.name).subscribe({
        next: (response: any) => {
          this.slides.splice(this.currentSlideIndex, 1);
          if (this.currentSlideIndex >= this.slides.length) {
            this.currentSlideIndex = Math.max(0, this.slides.length - 1);
          }
          alert(response.message);
          this.loadSlides();
        },
        error: (error) => console.error('Error deleting image:', error)
      });
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files?.[0];
    if (!file) {
      alert('Vui lòng chọn một file');
      return;
    }

    if (!file.type.startsWith('image/')) {
      alert('Chỉ chấp nhận file ảnh');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      alert('File không được vượt quá 10MB');
      return;
    }

    if (this.slides.length >= 5) {
      alert('Đã đạt giới hạn tối đa 5 ảnh. Vui lòng xóa bớt ảnh cũ.');
      return;
    }

    this.imageService.uploadImage(file).subscribe({
      next: (response: any) => {
        this.loadSlides();
        alert(response.message);
      },
      error: (error) => {
        alert(error.error || 'Có lỗi khi tải ảnh lên');
      }
    });
  }

  private loadSlides() {
    this.imageService.getListImage('home').subscribe({
      next: (response: any) => {
        this.slides = response.map((item: any) => ({
          name: item.name || item[0]?.name,
          url: item.url || item[1]?.url
        }));
      },
      error: (error) => console.error('Error loading slides:', error)
    });
  }

  loadDepartments(page: number = 0): void {
    this.departmentService.getDepartments(this.searchKeyword, page, this.limit)
      .subscribe({
        next: (response) => {
          this.departments = {
            items: response.objects,
            totalPages: response.total,
            currentPage: page
          }
        },
        error: (error) => {
          console.error('Error loading departments:', error);
        }
      });
  }

  onPageChange(status: true | false): void {
    if (status) {
      this.loadDepartments(this.departments ? this.departments.currentPage + 1 : 0);
    }
    else {
      this.loadDepartments(this.departments ? this.departments.currentPage - 1 : 0);
    }
  }

  handleImageError(event: any): void {
    event.target.src = 'assets/images/error-404.webp';
  }

  toggleStatus(id: number): void {
    this.departmentService.toggleStatusDepartment(id).subscribe({
      next: (response: any) => {
        alert(response.message);
        this.loadDepartments(this.departments ? this.departments.currentPage : 0);
      },
      error: (error: any) => {
        alert(error.error);
      }
    })
  }

  onFileSelectedDepartment(e: Event): void {
    const file = (e.target as HTMLInputElement).files?.[0] ?? null;
    if (!file || !file.type.startsWith('image/')) {
      alert('Chọn file ảnh hợp lệ.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      alert('Ảnh không quá 10 MB.');
      return;
    }
    
    this.image = file;
    const reader = new FileReader();
    reader.onload = () => {
      this.imagePreview = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  addNewDepartment(): void {
    if (!this.name || !this.description || !this.image) {
      alert('Vui lòng điền đầy đủ thông tin và chọn ảnh.');
      return;
    }

    this.departmentService.addDepartment(this.name, this.description, this.image).subscribe({
      next: (response: any) => {
        alert("Thêm phòng ban thành công!");
        this.loadDepartments();
        this.image = null;
        this.imagePreview = null; 
        this.name = '';
        this.description = '';
      },
      error: (error: any) => {
        alert(error.error);
      }
    });
  }
}
