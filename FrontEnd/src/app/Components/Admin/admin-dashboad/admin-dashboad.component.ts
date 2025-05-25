import { Component, OnInit } from '@angular/core';
import { ChartOptions, ChartType } from 'chart.js';
import { UserService, UserStats } from '../../../Services/user.service';
import { AppointmentService, AppointmentStats } from '../../../Services/appointment.service';
import { CommentService, CommentStats } from '../../../Services/comment.service';
import { ImageService } from '../../../Services/image.service';

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

  constructor(private userService: UserService,
              private appointmentService: AppointmentService,
              private commentService: CommentService,
              private imageService: ImageService 
  ) {}

  ngOnInit(): void {
    this.getInforNewUsers();
    this.getInforNewAppointments();
    this.getInforNewComments();
    this.loadSlides();
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
        next: () => {
          this.slides.splice(this.currentSlideIndex, 1);
          if (this.currentSlideIndex >= this.slides.length) {
            this.currentSlideIndex = Math.max(0, this.slides.length - 1);
          }
          alert('Xóa ảnh thành công');
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
        next: (response) => {
            console.log('Upload response:', response);
            this.loadSlides();
            alert('Tải ảnh lên thành công');
        },
        error: (error) => {
            console.error('Error uploading image:', error.error);
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
}
