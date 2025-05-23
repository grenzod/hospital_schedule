import { Component } from '@angular/core';
import { UserService } from '../../Services/user.service';
import { Router } from '@angular/router';

interface UserDTO {
  email: string;
  password: string;
}

@Component({
  selector: 'app-retrieve-password',
  templateUrl: './retrieve-password.component.html',
  styleUrl: './retrieve-password.component.css'
})
export class RetrievePasswordComponent {
  subject: string = '';
  password: string = '';
  showPassword: boolean = false;
  code: string = '';
  inputCode: boolean = false;
  retrieverData!: UserDTO;

  constructor(private userService: UserService, private router: Router) {}

  verify() {
    let email: string = '';
    if (this.isEmail(this.subject)) {
      email = this.subject
    }

    this.retrieverData = {
      email: email,
      password: this.password
    };

    this.userService.verify(this.retrieverData).subscribe({
      next: (response: any) => {
        alert(response.message);
        this.inputCode = true;
      },
      error: (err: any) => alert(err.error)
    });
  }

  retrieve() {
    this.userService.retrievePass(this.retrieverData, this.code).subscribe({
      next: () => {
        alert("Lấy lại mật khẩu thành công !");
        this.router.navigate(['/login']);
      },
      error: (err: any) => alert(err.error)
    });
  }

  togglePasswordVisibility(event: Event) {
    event.preventDefault();
    this.showPassword = !this.showPassword;
  }

  isEmail(subject: string): boolean {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(subject);
  }
}
