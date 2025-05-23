import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './Components/home/home.component';
import { SafePipe } from './Shared/safe.pipe';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { ToolbarComponent } from './Components/toolbar/toolbar.component';
import { TokenInterceptor } from './Interceptor/token.interceptor';
import { ProfileComponent } from './Components/profile/profile.component';
import { LoginComponent } from './Components/login/login.component';
import { BookComponent } from './Components/book/book.component';
import { DetailDoctorComponent } from './Components/detail-doctor/detail-doctor.component';
import { CommonModule } from '@angular/common';
import { AsisstantComponent } from './Components/assistant/assistant.component';
import { DoctorListComponent } from './Components/doctor-list/doctor-list.component';
import { IntroduceComponent } from './Components/introduce/introduce.component';
import { UserManageComponent } from './Components/Admin/user-manage/user-manage.component';
import { DoctorManageComponent } from './Components/Admin/doctor-manage/doctor-manage.component';
import { HomeAdminComponent } from './Components/Admin/home-admin/home-admin.component';
import { NewsComponent } from './Components/news/news.component';
import { NewsManageComponent } from './Components/Admin/news-manage/news-manage.component';
import { ArticleDetailComponent } from './Components/article-detail/article-detail.component';
import { AdminDashboadComponent } from './Components/Admin/admin-dashboad/admin-dashboad.component';
import { NgChartsModule } from 'ng2-charts'; 
import { DoctorManageAppointmentComponent } from './Components/Manage-appointment/doctor-manage-appointment/doctor-manage-appointment.component';
import { UserManageAppointmentComponent } from './Components/Manage-appointment/user-manage-appointment/user-manage-appointment.component';
import { AppointmentManageComponent } from './Components/Admin/appointment-manage/appointment-manage.component';
import { RegisterComponent } from './Components/register/register.component';
import { RetrievePasswordComponent } from './Components/retrieve-password/retrieve-password.component';
import { ServiceUserComponent } from './Components/service-user/service-user.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    AsisstantComponent,
    SafePipe,
    ToolbarComponent,
    ProfileComponent,
    LoginComponent,
    BookComponent,
    DetailDoctorComponent,
    DoctorListComponent,
    IntroduceComponent,
    UserManageComponent,
    DoctorManageComponent,
    HomeAdminComponent,
    NewsComponent,
    NewsManageComponent,
    ArticleDetailComponent,
    AdminDashboadComponent,
    DoctorManageAppointmentComponent,
    UserManageAppointmentComponent,
    AppointmentManageComponent,
    RegisterComponent,
    RetrievePasswordComponent,
    ServiceUserComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    CommonModule,
    NgChartsModule
  ],
  providers: [
    CookieService,
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
