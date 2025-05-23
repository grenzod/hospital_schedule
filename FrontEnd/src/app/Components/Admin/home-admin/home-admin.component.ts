import { Component } from '@angular/core';

@Component({
  selector: 'app-home-admin',
  templateUrl: './home-admin.component.html',
  styleUrls: ['./home-admin.component.css']
})
export class HomeAdminComponent {
  menuItems = [
    { key: '/admin/home',       icon: 'fas fa-home',      label: 'Quản lý chung' },
    { key: '/admin/user',       icon: 'fas fa-user',      label: 'Quản Lý Người Dùng' },
    { key: '/admin/doctor',     icon: 'fas fa-user-md',   label: 'Quản Lý Bác Sĩ' },
    { key: '/admin/news',       icon: 'fas fa-newspaper', label: 'Quản Lý Tin Tức' },
    { key: '/admin/appointment',icon: 'fas fa-calendar',  label: 'Quản Lý Đặt Lịch' },
  ];
}
