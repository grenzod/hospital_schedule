import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './Components/home/home.component';
import { AsisstantComponent } from './Components/assistant/assistant.component';
import { AuthGuard } from './Guards/author.guard';
import { BookComponent } from './Components/book/book.component';
import { ProfileComponent } from './Components/profile/profile.component';
import { LoginComponent } from './Components/login/login.component';
import { DetailDoctorComponent } from './Components/detail-doctor/detail-doctor.component';
import { DoctorListComponent } from './Components/doctor-list/doctor-list.component';
import { IntroduceComponent } from './Components/introduce/introduce.component';
import { UserManageComponent } from './Components/Admin/user-manage/user-manage.component';
import { AdminGuard } from './Guards/admin.guard';
import { NewsManageComponent } from './Components/Admin/news-manage/news-manage.component';
import { NewsComponent } from './Components/news/news.component';
import { ArticleDetailComponent } from './Components/article-detail/article-detail.component';
import { AdminDashboadComponent } from './Components/Admin/admin-dashboad/admin-dashboad.component';
import { DoctorManageComponent } from './Components/Admin/doctor-manage/doctor-manage.component';
import { DoctorManageAppointmentComponent } from './Components/Manage-appointment/doctor-manage-appointment/doctor-manage-appointment.component';
import { DoctorGuard } from './Guards/doctor.guard';
import { UserManageAppointmentComponent } from './Components/Manage-appointment/user-manage-appointment/user-manage-appointment.component';
import { AppointmentManageComponent } from './Components/Admin/appointment-manage/appointment-manage.component';
import { RegisterComponent } from './Components/register/register.component';
import { RetrievePasswordComponent } from './Components/retrieve-password/retrieve-password.component';
import { ServiceUserComponent } from './Components/service-user/service-user.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'booking', component: BookComponent },
  { path: 'introduce', component: IntroduceComponent },
  { path: 'profile/:id', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'asisstant', component: AsisstantComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'retrieve', component: RetrievePasswordComponent },
  { path: 'doctor/:id', component: DetailDoctorComponent },
  { path: 'professional/:id', component: DoctorListComponent },
  { path: 'service', component: ServiceUserComponent },
  { path: 'news', component: NewsComponent },
  { path: 'news/:id', component: ArticleDetailComponent },

  { path: 'appointment/doctor', component: DoctorManageAppointmentComponent, canActivate: [DoctorGuard] },
  { path: 'appointment/user', component: UserManageAppointmentComponent, canActivate: [AuthGuard] },

  { path: 'admin/home', component: AdminDashboadComponent, canActivate: [AdminGuard]},
  { path: 'admin/user', component: UserManageComponent, canActivate: [AdminGuard] },
  { path: 'admin/doctor', component: DoctorManageComponent, canActivate: [AdminGuard] },
  { path: 'admin/news', component: NewsManageComponent, canActivate: [AdminGuard] },
  { path: 'admin/appointment', component: AppointmentManageComponent, canActivate: [AdminGuard] },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
