import { Component, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { RegisterDTO, UserService } from '../../Services/user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  @ViewChild('registerForm') registerForm!: NgForm;

  subject: string = '';
  password: string = '';
  retypePassword: string = '';
  registerData!: RegisterDTO;
  showPassword: boolean = false;
  showRetypesPassword: boolean = false;
  code: string = '';
  inputCode: boolean = false;

  constructor(private router: Router,
    private userService: UserService
  ) { }

  verify() {
    let email: string = '';
    if (this.isEmail(this.subject)) {
      email = this.subject
    }

    this.registerData = {
      email: email,
      password: this.password,
      retype_password: this.retypePassword
    };

    if (this.password != this.retypePassword) {
      alert("Mật khẩu và nhập lại phải khớp với nhau");
    }
    else {
      this.userService.verify(this.registerData).subscribe({
        next: (response: any) => {
          alert(response.message);
          this.inputCode = true;
        },
        error: (err: any) => alert(err.error)
      });
    }
  }

  register() {
    this.userService.register(this.registerData, this.code).subscribe({
      next: () => {
        alert("Tạo tài khoản thành công !");
        this.router.navigate(['/login']);
      },
      error:(err: any) => alert(err.error)
    });
  }

  togglePasswordVisibility(event: Event, type: string = 'password') {
    event.preventDefault();
    if(type == 'password') this.showPassword = !this.showPassword;
    else this.showRetypesPassword = !this.showRetypesPassword;
  }

  isEmail(subject: string): boolean {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(subject);
  }
}
