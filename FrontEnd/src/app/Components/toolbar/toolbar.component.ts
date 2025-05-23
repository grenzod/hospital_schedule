import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { UserResponse } from '../../Models/userResponse';
import { UserService } from '../../Services/user.service';
import { TokenService } from '../../Services/token.service';

@Component({
    selector: 'app-toolbar',
    templateUrl: './toolbar.component.html',
    styleUrl: './toolbar.component.css',
})
export class ToolbarComponent implements OnInit {
    activeMenu: string = 'home';
    userResponse!: UserResponse | null;
    menuItems: any[] = [];
    @ViewChild('dropdownButton') dropdownButton!: ElementRef;
    @ViewChild('dropdownMenu') dropdownMenu!: ElementRef;

    private baseMenuItems = [
        {
            key: '/',
            icon: 'fas fa-home',
            label: 'Trang Chủ'
        },
        {
            key: '/introduce',
            icon: 'fas fa-info-circle',
            label: 'Giới Thiệu'
        },
        {
            key: '/booking',
            icon: 'fas fa-calendar',
            label: 'Đặt Lịch Khám'
        },
        {
            key: '/professional/0',
            icon: 'fas fa-user-md',
            label: 'Đội Ngũ Chuyên Gia'
        },
        {
            key: '/service',
            icon: 'fas fa-stethoscope',
            label: 'Dịch Vụ Y Tế'
        },
        {
            key: '/asisstant',
            icon: 'fas fa-comments',
            label: 'Tư Vấn Trợ Lý Ảo'
        },
        {
            key: '/news',
            icon: 'fas fa-newspaper',
            label: 'Tin Tức'
        },
        {
            key: 'https://www.facebook.com/BenhVienDaKhoaMedlatec/',
            icon: 'fas fa-phone',
            label: 'Liên Hệ'
        },
    ];

    constructor(
        private router: Router,
        private userService: UserService,
        private tokenService: TokenService
    ) {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
            this.updateMenuItems();
        });
    }

    ngOnInit() {
        this.userResponse = this.userService.getUserFromCookie();
        this.updateMenuItems();
    }

    isDropdownOpen = false;

    @HostListener('document:click', ['$event'])
    clickout(event: any) {
        if (!this.dropdownButton?.nativeElement.contains(event.target) &&
            !this.dropdownMenu?.nativeElement.contains(event.target)) {
            this.isDropdownOpen = false;
        }
    }

    toggleDropdown() {
        this.isDropdownOpen = !this.isDropdownOpen;
    }

    private updateMenuItems() {
        this.userResponse = this.userService.getUserFromCookie();
        this.menuItems = [...this.baseMenuItems];

        if (this.userResponse) {
            this.menuItems.push({
                key: `profile/${this.userResponse.id}`,
                icon: 'fas fa-user',
                label: 'Hồ Sơ'
            });
        }
    }

    logout() {
        this.userService.logout(this.tokenService.getToken()).subscribe({
            next: () => {
                this.handleLogout();
            },
            error: (error) => {
                console.error('Logout failed:', error);
                this.handleLogout();
            }
        });
    }

    private handleLogout() {
        this.tokenService.clearToken();
        this.userService.clearUserInfo();
        this.userResponse = null;
        this.updateMenuItems();
        this.router.navigate(['/login']);
    }
}
