import { Observable } from "rxjs";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";

export interface CommentStats {
    year: number;
    month: number;
    count: number;
}

@Injectable({
    providedIn: 'root'
})
export class CommentService {
    private api = `${environment.apiBaseUrl}/reviews`;

    constructor(private http: HttpClient) { }

    getAllReviewsByDoctorId(page: number = 0, limit: number = 10, doctorId: number): Observable<any> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('limit', limit.toString())
            .set('doctorId', doctorId.toString());
        return this.http.get<any>(this.api, { params });
    }

    addAFeedBack(userId: number, doctorId: number, feedback: string): Observable<any> {
        const params = new HttpParams()
            .set('userId', userId.toString())
            .set('doctorId', doctorId.toString())
            .set('comment', feedback);
        return this.http.post<any>(`${this.api}/feedback`, null, { params: params });
    }

    getAnalyzeComments(months: number = 6): Observable<CommentStats[]> {
        const params = new HttpParams()
            .set("months", months.toString())
        return this.http.get<CommentStats[]>(`${this.api}/analyze`, { params });
    }

    deleteComment(id: number): Observable<string> {
        return this.http.delete<string>(`${this.api}/${id}`);
    }
}
