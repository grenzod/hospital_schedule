import { Component, OnInit, OnDestroy } from '@angular/core';
import { environment } from '../../Environments/environment';
import { ImageService } from '../../Services/image.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit, OnDestroy {
  serviceImages = [
    '../../../assets/images/service1.png',
    '../../../assets/images/service2.png',
    '../../../assets/images/service3.png',
    '../../../assets/images/service4.png'
  ];
  slides: string[] = [];

  mapUrl = environment.urlMap;
  currentSlide = 0;
  private slideInterval: any;

  constructor(private imageService: ImageService) { }

  ngOnInit() {
    this.getListImageUrl('home');
    this.startSlideShow();
  }

  ngOnDestroy() {
    if (this.slideInterval) {
      clearInterval(this.slideInterval);
    }
  }

  startSlideShow() {
    this.slideInterval = setInterval(() => {
      this.currentSlide = (this.currentSlide + 1) % this.slides.length;
    }, 3000);
  }

  currentServiceImage = this.serviceImages[0];
  activeService: number = 0;

  selectService(index: number) {
    this.currentServiceImage = this.serviceImages[index];
  }

  setActiveService(index: number) {
    this.activeService = index;
    this.selectService(index);
  }

  nextSlide() {
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;
    this.resetTimer();
  }

  previousSlide() {
    this.currentSlide = this.currentSlide === 0
      ? this.slides.length - 1
      : this.currentSlide - 1;
    this.resetTimer();
  }

  goToSlide(index: number) {
    this.currentSlide = index;
    this.resetTimer();
  }

  private resetTimer() {
    if (this.slideInterval) {
      clearInterval(this.slideInterval);
      this.startSlideShow();
    }
  }

  private getListImageUrl(nameFolder: string) {
    this.imageService.getListImageUrl(nameFolder)
      .subscribe({
        next: (urls: string[]) => {
          this.slides = urls;
        },
        error: err => {
          console.error('Lỗi tải ảnh:', err);
        }
      });
  }
}