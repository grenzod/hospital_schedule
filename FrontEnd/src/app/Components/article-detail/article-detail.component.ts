import { Component, OnInit } from '@angular/core';
import { Article } from '../../Models/article';
import { ArticleService } from '../../Services/articles.service';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-article-detail',
  templateUrl: './article-detail.component.html',
  styleUrls: ['./article-detail.component.css']
})
export class ArticleDetailComponent implements OnInit {
  article!: Article;
  errorMessage?: string;
  isLoading = true;
  id!: string;

  constructor(
    private articleService: ArticleService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')!;
    this.getArticleById();
  }

  private getArticleById(): void {
    this.articleService.getArticleById(this.id)
      .subscribe({
        next: (article: Article) => {
          this.article = article;
          this.isLoading = false;
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 404) {
            this.errorMessage = 'Không tìm thấy bài viết.';
          } else {
            this.errorMessage = err.error?.message || 'Đã xảy ra lỗi khi tải bài viết.';
          }
          this.isLoading = false;
        }
      });
  }
}
