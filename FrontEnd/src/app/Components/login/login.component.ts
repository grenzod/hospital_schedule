import { Component, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { UserResponse } from '../../Models/userResponse';
import { UserService } from '../../Services/user.service';
import { TokenService } from '../../Services/token.service';
import { LoginDTO } from '../../DTOs/user/login.DTO';
import { LoginResponse } from '../../Models/loginResponse';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrl: './login.component.css',
})
export class LoginComponent {
    @ViewChild('loginForm') loginForm!: NgForm;

    subject: string = '';
    password: string = '';
    showPassword: boolean = false;
    userResponse!: UserResponse;

    constructor(private router: Router,
        private userService: UserService,
        private tokenService: TokenService,
    ) { }

    login() {
        debugger
        let email: string = '';
        if (this.isEmail(this.subject)) {
            email = this.subject
        }

        const loginData: LoginDTO = {
            email: email,
            password: this.password
        };

        this.userService.login(loginData).subscribe({
            next: (responseT: LoginResponse) => {
                debugger
                const token = responseT.token;
                this.tokenService.setToken(token, new Date(Date.now() + 60 * 60 * 1000));
                this.userService.getUserDetail(token).subscribe({
                    next: (response: any) => {
                        debugger
                        this.userResponse = {
                            ...response,
                            date_of_birth: new Date(response.date_of_birth),
                        }
                        this.userService.saveUserResponse(this.userResponse);
                        if(this.userResponse.role.name === 'admin') this.router.navigate(['/admin/home']);
                        else this.router.navigate(['/']);
                    },
                    complete: () => {
                        debugger
                    },
                    error: (err: any) => {
                        debugger
                        console.error('Error getting user detail: ', err);
                    }
                })
            },
            complete: () => {
                debugger;
            },
            error: (responseT: LoginResponse) => {
                debugger
                window.alert(`Can not login, error: ${responseT.message}`);
            }
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
