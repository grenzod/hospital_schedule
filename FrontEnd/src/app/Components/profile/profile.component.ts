import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserDTO } from '../../DTOs/user/user.DTO';
import { Gender } from '../../Enums/gender.enum';
import { UserResponse } from '../../Models/userResponse';
import { UserService } from '../../Services/user.service';
import { TokenService } from '../../Services/token.service';

interface Profile {
  avatar_url: string;
  phoneNumber: string;
  fullName: string;
  gender: Gender;
  hometown: string;
  birthDate: string;
}

export interface ChangePass {
  password: string;
  retype_password: string;
}

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  profile: Profile = {
    avatar_url: '',
    phoneNumber: '',
    fullName: '',
    gender: Gender.OTHER,
    hometown: '',
    birthDate: ''
  };
  userResponse!: UserResponse | null;
  userId!: number;
  genderOptions = Object.values(Gender);
  Gender = Gender;
  file: File | null = null;
  oldPassword: string = "";
  newPassword: string = "";
  showOldPassword: boolean = false;
  showNewPassword: boolean = false;

  constructor(private userService: UserService,
    private activeRoute: ActivatedRoute,
    private tokenService: TokenService) { }

  ngOnInit(): void {
    this.userResponse = this.userService.getUserFromCookie();
    this.activeRoute.paramMap.subscribe((params) => {
      this.userId = Number(params.get('id'));
    });
    this.loadProfileData();
  }

  loadProfileData() {
    this.profile = {
      avatar_url: this.userResponse?.avatar_url || '',
      phoneNumber: this.userResponse?.phone_number || '',
      fullName: this.userResponse?.full_name || '',
      gender: this.userResponse?.gender as Gender || Gender.OTHER,
      hometown: this.userResponse?.address || '',
      birthDate: this.userResponse?.date_of_birth ? new Date(this.userResponse.date_of_birth).toISOString().split('T')[0] : ''
    };
  }

  saveProfile() {
    const token = this.tokenService?.getToken() || '';
    const userDTO: UserDTO = {
      phone_number: this.profile.phoneNumber,
      full_name: this.profile.fullName,
      gender: this.profile.gender,
      address: this.profile.hometown,
      date_of_birth: this.profile.birthDate
    };

    this.userService.updateUser(this.userId, token, this.file, userDTO).subscribe({
      next: (response) => {
        this.userResponse = {
          ...response,
          date_of_birth: new Date(response.date_of_birth),
        };
        if (this.userResponse) {
          this.userService.saveUserResponse(this.userResponse);
        }
        else {
          console.error('User response is null after update');
        }
      },
      complete: () => {
        this.loadProfileData();
        alert('Profile updated successfully!');
      },
      error: (error) => {
        console.error('Error updating profile:', error);
      }
    });
  }

  handleImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/default-avatar.webp';
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.file = input.files?.[0] || null;
  }

  togglePasswordVisibility(event: Event, type: 'old' | 'new'): void {
    event.preventDefault();
    if (type === 'old') {
      this.showOldPassword = !this.showOldPassword;
    } else if (type === 'new') {
      this.showNewPassword = !this.showNewPassword;
    }
  }

  changePassword(): void {
    const token = this.tokenService?.getToken() || '';
    const userDTO: ChangePass = {
      password: this.oldPassword,
      retype_password: this.newPassword
    };
    this.userService.changePassword(this.userId, userDTO, token).subscribe({
      next: (response) => {
        this.userResponse = {
          ...response,
          date_of_birth: new Date(response.date_of_birth),
        };
        if (this.userResponse) {
          this.userService.saveUserResponse(this.userResponse);
        }
        else {
          console.error('User response is null after update');
        }
      },
      complete: () => {
        alert('Password changed successfully!');
      },
      error: (error) => {
        console.error('Error changing password:', error);
      }
    });
  }
}
