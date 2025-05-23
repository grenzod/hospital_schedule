import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../Environments/environment';
import { ImageService } from './image.service';

@Injectable({
    providedIn: 'root'
})
export class DepartmentService {
    private apiUrl = `${environment.apiBaseUrl}`;

    constructor(private http: HttpClient,
                private imageService: ImageService
    ) { }

    getDepartments(keyword: string = '', page: number = 0, limit: number = 10): Observable<any> {
        let params = new HttpParams()
            .set('keyword', keyword)
            .set('page', page.toString())
            .set('limit', limit.toString());

        return this.http.get(`${this.apiUrl}/departments`, { params })
            .pipe(
                map((response: any) => ({
                    ...response,
                    objects: response.objects.map((dept: any) => ({
                        ...dept,
                        thumbnail_url: this.imageService.getImageUrl("departments", dept.thumbnail_url)
                    }))
                }))
            );
    }
}