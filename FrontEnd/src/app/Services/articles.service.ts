import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { Observable } from "rxjs";
import { Article } from "../Models/article";
import { ImageService } from "./image.service";
import { map } from 'rxjs/operators';
import { ContentBlock } from "../Models/contentBlock";
import { ObjectListResponse } from "../Models/objectListResponse";

@Injectable({
    providedIn: 'root'
})
export class ArticleService {
    private api = `${environment.apiBaseUrl}/articles`;

    constructor(private http: HttpClient,
        private imageService: ImageService
    ) { }

    createArticle(formData: FormData): Observable<Article> {
        return this.http.post<Article>(this.api, formData);
    }

    getAllArticles(
        page: number = 0,
        limit: number = 10,
        keyword: string = '',
        status: boolean = true
      ): Observable<{ articles: Article[]; totalPages: number }> {
        const params = new HttpParams()
          .set('page', page.toString())
          .set('limit', limit.toString())
          .set('keyword', keyword)
          .set('status', String(status));
    
        return this.http
          .get<ObjectListResponse<Article>>(`${this.api}/`, { params })
          .pipe(
            map(resp => ({
              articles: resp.objects.map(a => ({
                ...a,
                thumbnail: this.imageService.getImageUrl('thumbnailArticle', a.thumbnail)
              })),
              totalPages: resp.total
            }))
          );
      }


    getArticleById(id: string): Observable<Article> {
        return this.http.get<Article>(`${this.api}/` + id)
            .pipe(
                map((response: any) => ({
                    ...response,
                    contentBlocks: response.contentBlocks
                        .map((block: ContentBlock) => ({
                            ...block,
                            image: block.type === 'image' ? {
                                ...block.image,
                                url: this.imageService.getImageUrl('articles', block.image.url)
                            }
                                : null
                        }))
                }))
            );
    }

    deleteArticleById(id: string): Observable<string> {
      return this.http.delete<string>(`${this.api}/` + id);
    }
}