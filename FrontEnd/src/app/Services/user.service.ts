import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { catchError } from 'rxjs/operators';
import { CookieService } from "ngx-cookie-service";
import { LoginDTO } from "../DTOs/user/login.DTO";
import { UserDTO } from "../DTOs/user/user.DTO";
import { UserResponse } from "../Models/userResponse";
import { ImageService } from "./image.service";
import { map } from 'rxjs/operators';
import { ChangePass } from "../Components/profile/profile.component";

export interface RegisterDTO {
    email: string;
    password: string;
    retype_password: string;
}

export interface UserStats {
    year: number;
    month: number;
    count: number;
}

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiBaseUrl}/users`;
    private readonly COOKIE_DOMAIN = environment.cookieDomain;
    private readonly USER_KEY = 'User';

    private constructor(private http: HttpClient,
        private cookieService: CookieService,
        private imageService: ImageService
    ) { }

    getAnalyzeUsers(months: number = 6): Observable<UserStats[]> {
        const params = new HttpParams()
            .set("months", months.toString())
        return this.http.get<UserStats[]>(`${this.apiUrl}/analyze`, { params });
    }

    verify(registerData: any): Observable<any> {
        return this.http.post(this.apiUrl + "/verify", registerData, {});
    }

    register(registerData: RegisterDTO, code: string): Observable<any> {
        const params = new HttpParams().set('code', code);
        return this.http.post(this.apiUrl + "/register", registerData, { params });
    }

    retrievePass(retrieverData: any, code: string) {
        const params = new HttpParams().set('code', code);
        return this.http.post(this.apiUrl + "/register", retrieverData, { params });
    }

    login(loginData: LoginDTO): Observable<any> {
        return this.http.post(this.apiUrl + "/login", loginData, {
            headers: new HttpHeaders({
                'X-User-Agent': 'web'
            })
        });
    }

    logout(token: string | null): Observable<any> {
        if (!token) {
            return new Observable(observer => {
                observer.error(new Error('No authentication token available'));
            });
        }

        return this.http.post(this.apiUrl + "/logout", {}, {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            })
        }).pipe(
            catchError(error => {
                console.error('Logout error:', error);
                throw new Error(error.message || 'Logout failed');
            })
        );
    }

    getUserDetail(token: string): Observable<any> {
        return this.http.post(this.apiUrl + "/details", {}, {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            })
        }).pipe(
            map((response: any) => ({
                ...response,
                avatar_url: this.imageService.getImageUrl("users", response.avatar_url)
            }))
        );
    }

    getAllUsers(keyword: string = '', page: number = 0, limit: number = 10): Observable<any> {
        let params = new HttpParams()
            .set('keyword', keyword)
            .set('page', page.toString())
            .set('limit', limit.toString());
        return this.http.get<any>(`${this.apiUrl}`, { params })
            .pipe(
                map((response: any) => ({
                    ...response,
                    objects: response.objects.map((user: UserResponse) => ({
                        ...user,
                        avatar_url: this.imageService.getImageUrl("users", user.avatar_url),
                    }))
                }))
            );
    }

    updateUser(id: number, token: string, file: File | null, userData: UserDTO): Observable<any> {
        const formData = new FormData();
        formData.append('user', new Blob([JSON.stringify(userData)], { type: 'application/json' }));
        if (file) {
            formData.append('file', file, file.name);
        }
        const headers = new HttpHeaders({
            Authorization: `Bearer ${token}`
        });
        return this.http.put(`${this.apiUrl}/updates/${id}`, formData, { headers });
    }

    toggleUserStatus(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/toggle/${id}`)
    }

    changePassword(id: number, userDTO: ChangePass, token: string): Observable<any> {
        return this.http.put(`${this.apiUrl}/change-password/${id}`, userDTO, {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            })
        });
    }

    saveUserResponse(userResponse: UserResponse): void {
        const userString = JSON.stringify(userResponse);
        this.cookieService.set(
            this.USER_KEY,
            userString,
            {
                expires: new Date(Date.now() + 60 * 60 * 1000),
                secure: true,
                sameSite: 'Strict',
                domain: this.COOKIE_DOMAIN,
                path: '/'
            }
        );
    }

    getUserFromCookie(): UserResponse | null {
        try {
            const userString = this.cookieService.get(this.USER_KEY);
            if (!userString) return null;

            console.log('Cookie value:', userString);

            return JSON.parse(userString) as UserResponse;
        } catch (error) {
            console.error('Error parsing user from cookie:', error);
            return null;
        }
    }

    clearUserInfo(): void {
        this.cookieService.delete(
            this.USER_KEY,
            '/',
            this.COOKIE_DOMAIN,
            true,
            'Strict'
        );
    }
}