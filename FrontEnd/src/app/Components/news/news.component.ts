import { Component, OnInit } from '@angular/core';
import { Article } from '../../Models/article';
import { ArticleService } from '../../Services/articles.service';
import { UserService } from '../../Services/user.service';

@Component({
  selector: 'app-news',
  templateUrl: './news.component.html',
  styleUrls: ['./news.component.css']
})
export class NewsComponent implements OnInit {
  articles: Article[] = [];
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  role!: string;
  keyword: string = '';

  constructor(private articleService: ArticleService, 
              private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadPage(this.currentPage);
    this.role = this.userService.getUserFromCookie()!.role.name;
  }

  loadPage(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.articleService.getAllArticles(this.currentPage, this.pageSize, this.keyword)
      .subscribe(({ articles, totalPages }) => {
        this.articles = articles;
        this.totalPages = totalPages;
      });
  }

  get pages(): number[] {
    return Array(this.totalPages)
      .fill(0)
      .map((_, i) => i);
  }

  delete(id: string) {
    this.articleService.getArticleById(id).subscribe((response: any) => {
      alert(response);
    });
  }
}
