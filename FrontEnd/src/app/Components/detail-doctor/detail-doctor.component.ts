import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DoctorResponse } from '../../Models/doctorResponse';
import { DoctorService } from '../../Services/doctor.service';
import { AppointmentService, Appointment } from '../../Services/appointment.service';
import { UserService } from '../../Services/user.service';
import { CommentService } from '../../Services/comment.service';
import { ReviewResponse } from '../../Models/reviewResponse';
import { Role } from '../../Models/role';

@Component({
  selector: 'app-detail-doctor',
  templateUrl: './detail-doctor.component.html',
  styleUrls: ['./detail-doctor.component.css']
})
export class DetailDoctorComponent implements OnInit {
  bookingForm!: FormGroup;
  commentForm!: FormGroup;
  timeSlots = ['08:00', '09:00', '10:00', '11:00', '13:00', '14:00', '15:00', '16:00'];
  doctor?: DoctorResponse;
  comments: ReviewResponse[] = [];
  doctorId!: number;
  currentPage: number = 0;
  limit: number = 10;
  totalPages: number = 1;
  role: Role | null = null;

  showBookingForm: boolean = false;

  constructor(
    private fb: FormBuilder,
    private activeRoute: ActivatedRoute,
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private userService: UserService,
    private commentService: CommentService
  ) { }

  ngOnInit() {
    this.doctorId = +this.activeRoute.snapshot.paramMap.get('id')!;
    const user = this.userService.getUserFromCookie();
    if (user) {
      this.role = user.role;
    }
    this.getAllReviewsByDoctorId();
    this.getDoctorById();

    this.bookingForm = this.fb.group({
      date: ['', Validators.required],
      time: ['', Validators.required],
      name: [this.userService.getUserFromCookie()?.full_name, Validators.required],
      phone: [this.userService.getUserFromCookie()?.phone_number, [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      symptoms: ['']
    });

    this.commentForm = this.fb.group({
      name: [this.userService.getUserFromCookie()?.full_name, Validators.required],
      comment: ['', Validators.required]
    });
  }

  getDoctorById() {
    this.doctorService.getDoctorById(this.doctorId)
      .subscribe((doctor: DoctorResponse) => {
        this.doctor = doctor;
      });
  }

  getAllReviewsByDoctorId() {
    this.commentService.getAllReviewsByDoctorId(this.currentPage, this.limit, this.doctorId)
      .subscribe((response: any) => {
        this.comments = response.objects;
        this.totalPages = response.total;
      });
  }

  toggleBookingForm() {
    if (this.role == null) {
      alert('Bạn cần đăng nhập để đặt lịch hẹn!');
      return;
    }
    this.showBookingForm = !this.showBookingForm;
  }

  submitBooking() {
    if (this.bookingForm.valid && this.doctor) {
      const appointmentData: Appointment = {
        userId: this.userService.getUserFromCookie()?.id!,
        doctorId: this.doctor.id,
        appointmentDate: this.bookingForm.get('date')?.value,
        appointmentTime: this.bookingForm.get('time')?.value,
        full_name: this.bookingForm.get('name')?.value,
        phone_number: this.bookingForm.get('phone')?.value,
        symptoms: this.bookingForm.get('symptoms')?.value
      };

      this.appointmentService.scheduleAppointment(appointmentData)
        .subscribe(
          response => {
            console.log('Đặt lịch thành công:', response);
            alert('Đặt lịch thành công!');
          },
          error => {
            console.error('Lỗi đặt lịch:', error);
            alert('Lỗi đặt lịch: ' + error.error);
          }
        );
    }
  }

  submitComment() {
    if (this.role == null) {
      alert('Bạn cần đăng nhập để bình luận!');
      return;
    }
    if (!this.commentForm.valid) {
        alert('Vui lòng nhập bình luận hợp lệ.');
        return;
    }
    if (this.commentForm.valid) {
      const userId = this.userService.getUserFromCookie()?.id ?? 0;
      const doctorId = this.doctorId;
      const feedback = this.commentForm.get('comment')?.value;
      this.commentService.addAFeedBack(userId, doctorId, feedback)
        .subscribe(
          response => {
            console.log('Feed back successfully !!', response);
            alert('Feed back successfully !!');
            this.getAllReviewsByDoctorId();
            this.commentForm.reset();
          },
          error => {
            console.error('Lỗi đánh giá:', error);
            alert('Feed back error: ' + error.error);
          }
        );
    }
  }

  deleteComment(id: number) {
    if (this.role != null && this.role.name == 'admin') {
      this.commentService.deleteComment(id).subscribe(
        (response: any) => {
          alert(response);
          this.getAllReviewsByDoctorId();
        }
      );
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.getAllReviewsByDoctorId();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.getAllReviewsByDoctorId();
    }
  }
}
