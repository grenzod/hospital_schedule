import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable, catchError, throwError } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ImageService {
    private apiUrl = `${environment.apiBaseUrl}`;

    private constructor(private http: HttpClient) { }

    getImageUrl(nameFolder: string, imageName: string): string {
        const encodedImageName = encodeURIComponent(imageName);
        return `${this.apiUrl}/images/${encodedImageName}?folder=${nameFolder}`;
    }

    getListImageUrl(nameFolder: string): Observable<any> {
        const params = new HttpParams()
            .set('folder', nameFolder);
        return this.http.get<any>(`${this.apiUrl}/images/list`, { params });
    }

    getListImage(nameFolder: string): Observable<any> {
        const params = new HttpParams()
            .set('folder', nameFolder);
        return this.http.get<any>(`${this.apiUrl}/home/list`, { params });
    }

    deleteImageByName(name: string): Observable<any> {
        const params = new HttpParams()
            .set('imageName', name);
        return this.http.delete<any>(`${this.apiUrl}/home`, { params });
    }

    uploadImage(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        
        return this.http.post<any>(`${this.apiUrl}/home`, formData, {
            reportProgress: true,
            observe: 'response'
        }).pipe(
            catchError(error => {
                console.error('Upload error:', error);
                return throwError(() => error);
            })
        );
    }
}