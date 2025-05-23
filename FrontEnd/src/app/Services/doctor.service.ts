import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { map } from 'rxjs/operators';
import { ImageService } from "./image.service";
import { DoctorResponse } from "../Models/doctorResponse";

@Injectable({
    providedIn: 'root'
})
export class DoctorService {
    private api = `${environment.apiBaseUrl}/doctors`;

    constructor(private http: HttpClient,
        private imageService: ImageService
    ) { }

    getAllDoctors(keyword: string = '', departmentId: number | null, status: boolean | null, page: number = 0, limit: number = 10): Observable<any> {
        let params = new HttpParams()
            // .set('keyword', keyword)
            // .set('department', departmentId)
            // .set('status', status)
            .set('page', page.toString())
            .set('limit', limit.toString());
        if (keyword.trim().length) {
            params = params.set('keyword', keyword.trim());
        }
        if (departmentId !== null) {
            params = params.set('department', departmentId.toString());
        }
        if (status!== null) {
            params = params.set('status', status.toString());
        }
        return this.http.get<any>(`${this.api}`, { params })
            .pipe(
                map((response: any) => ({
                    ...response,
                    objects: response.objects.map((doctor: DoctorResponse) => ({
                        ...doctor,
                        avatar_url: this.imageService.getImageUrl("doctors", doctor.avatar_url)
                    }))
                }))
            );
    }

    getDoctorByDepartmentId(departmentId: number, page: number = 0, limit: number = 10): Observable<any> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('limit', limit.toString());
        return this.http.get<any>(`${this.api}/getByDepartment/${departmentId}`, { params })
            .pipe(
                map((response: any) => ({
                    ...response,
                    objects: response.objects.map((doctor: DoctorResponse) => ({
                        ...doctor,
                        avatar_url: this.imageService.getImageUrl("doctors", doctor.avatar_url)
                    }))
                }))
            );;
    }

    getDoctorById(id: number): Observable<DoctorResponse> {
        return this.http.get<DoctorResponse>(`${this.api}/getDoctor/${id}`)
            .pipe(
                map((doctor: DoctorResponse) => ({
                    ...doctor,
                    avatar_url: this.imageService.getImageUrl("doctors", doctor.avatar_url)
                })
                )
            );
    }

    addDoctor(userId: number, departmentId: number, licenseNumber: number, file: File): Observable<any> {
        const formData = new FormData();
        formData.append('ids', `${userId},${departmentId}`);
        formData.append('number', licenseNumber.toString());
        formData.append('file', file, file.name);
        return this.http.post<any>(`${this.api}/add`, formData);
    }

    toggleStatusDoctor(id: number): Observable<any> {
        return this.http.delete<any>(`${this.api}/delete/${id}`);
    }
}
