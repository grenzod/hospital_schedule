import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ArticleService } from '../../../Services/articles.service';
import { Article } from '../../../Models/article';

@Component({
  selector: 'app-news-manage',
  templateUrl: './news-manage.component.html',
  styleUrls: ['./news-manage.component.css']
})
export class NewsManageComponent {
  articleForm: FormGroup;
  thumbnailPreview?: string;
  previewImages: Record<number, string> = {};
  selectedFiles: File[] = [];
  errorMessage: string | null = null;
  isLoading = false;
  articles: Article[] = [];
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  keyword: string = '';

  constructor(private fb: FormBuilder, private articleService: ArticleService) {
    this.articleForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      thumbnail: [null, Validators.required],
      contentBlocks: this.fb.array([], [Validators.required, Validators.minLength(1)])
    });
    this.loadPage(this.currentPage);
  }

  get contentBlocks(): FormArray {
    return this.articleForm.get('contentBlocks') as FormArray;
  }

  addTextBlock(): void {
    this.contentBlocks.push(
      this.fb.group({
        type: ['text'],
        text: ['', Validators.required],
        image: this.fb.group({ caption: [''] })
      })
    );
  }

  addImageBlock(): void {
    this.contentBlocks.push(
      this.fb.group({
        type: ['image'],
        caption: ['', Validators.required]
      })
    );
  }

  removeBlock(index: number): void {
    this.contentBlocks.removeAt(index);
    this.selectedFiles.splice(index, 1);
    delete this.previewImages[index];
  }

  onTypeChange(idx: number): void {
    const block = this.contentBlocks.at(idx);
    const isText = block.get('type')?.value === 'text';
    if (isText) {
      block.get('text')?.setValidators([Validators.required]);
      block.get('caption')?.clearValidators();
    } else {
      block.get('caption')?.setValidators([Validators.required]);
      block.get('text')?.clearValidators();
    }
    block.updateValueAndValidity();
  }

  onThumbnailChange(e: Event): void {
    const file = (e.target as HTMLInputElement).files?.[0] ?? null;
    const ctrl = this.articleForm.get('thumbnail')!;
    if (!file || !file.type.startsWith('image/')) {
      this.errorMessage = 'Chọn file ảnh hợp lệ.';
      ctrl.setValue(null);
      this.thumbnailPreview = undefined;
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'Ảnh không quá 5 MB.';
      ctrl.setValue(null);
      this.thumbnailPreview = undefined;
      return;
    }
    this.errorMessage = null;
    ctrl.setValue(file);
    const reader = new FileReader();
    reader.onload = () => this.thumbnailPreview = reader.result as string;
    reader.readAsDataURL(file);
  }

  onFileSelected(e: Event, idx: number): void {
    const file = (e.target as HTMLInputElement).files?.[0] ?? null;
    if (!file || !file.type.startsWith('image/')) {
      this.errorMessage = 'Chọn file ảnh hợp lệ.';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.errorMessage = 'Ảnh không quá 10 MB.';
      return;
    }
    this.errorMessage = null;
    this.selectedFiles[idx] = file;
    const reader = new FileReader();
    reader.onload = () => this.previewImages[idx] = reader.result as string;
    reader.readAsDataURL(file);
  }

  private validate(): boolean {
    if (this.articleForm.invalid) {
      this.articleForm.markAllAsTouched();
      this.errorMessage = 'Vui lòng hoàn thiện thông tin.';
      return false;
    }

    for (let i = 0; i < this.contentBlocks.length; i++) {
      if (this.contentBlocks.at(i).get('type')?.value === 'image' && !this.selectedFiles[i]) {
        this.errorMessage = 'Chọn ảnh cho mọi khối image.';
        return false;
      }
    }
    return true;
  }

  onSubmit(): void {
    this.errorMessage = null;
    if (!this.validate()) return;
    this.isLoading = true;

    const formData = new FormData();
    const thumb: File = this.articleForm.get('thumbnail')!.value;
    formData.append('thumbnail', thumb);
    const payload = {
      title: this.articleForm.value.title,
      content_block: this.contentBlocks.controls.map((blk, i) => ({
        type: blk.get('type')!.value,
        text: blk.get('text')?.value ?? '',
        image: blk.get('type')!.value === 'image'
          ? { caption: blk.get('caption')!.value, index: i }
          : null
      }))
    };
    formData.append('articles', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    this.selectedFiles.forEach((f, i) => {
      if (f) formData.append('files', f);
    });

    this.articleService.createArticle(formData).subscribe({
      next: () => {
        alert('Tạo bài viết thành công!');
        this.articleForm.reset();
        this.thumbnailPreview = undefined;
        this.contentBlocks.clear();
        this.selectedFiles = [];
        this.previewImages = {};
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Lỗi khi tạo bài viết.';
        this.isLoading = false;
      },
      complete: () => this.isLoading = false
    });
  }

  loadPage(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.articleService.getAllArticles(this.currentPage, this.pageSize, this.keyword, false)
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
