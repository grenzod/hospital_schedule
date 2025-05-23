import { Component, ViewChild, ElementRef, AfterViewInit, HostListener, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { AssistantService, Suggestion } from '../../Services/assistant.service';

export interface Message {
  role: 'user' | 'assistant';
  content: string;
}

@Component({
  selector: 'app-asisstant',
  templateUrl: './assistant.component.html',
  styleUrl: './assistant.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AsisstantComponent implements AfterViewInit {
  @ViewChild('chatContainer') private chatContainer!: ElementRef;
  @ViewChild('inputBox') inputBox!: ElementRef<HTMLTextAreaElement>;

  newMessage: string = '';
  messages: Message[] = [];
  suggestion: string = '';
  isLoading: boolean = false;
  showSuggestions: boolean = true;
  suggestionQuestions: string[] = [
    'How can I have a healthy diet ?',
    'Who is the person everyone likes the most ?'
  ];

  private inputSubject = new Subject<string>();

  constructor(private assistantService: AssistantService, private cdr: ChangeDetectorRef) { }

  ngAfterViewInit() {
    this.assistantService.connect();
    this.assistantService.onSuggestion().subscribe((sug: Suggestion) => {
      this.suggestion = sug.token ? sug.token : '';
      this.cdr.detectChanges();
      console.log('Current Suggestion:', this.suggestion);
      console.log('Current message:', this.newMessage);
    });
    console.log('Current Suggestion:', this.suggestion);

    this.inputSubject.pipe(debounceTime(300)).subscribe(text => {
      this.assistantService.send(text);
    });
  }

  ngOnInit() {
    this.showSuggestions = this.messages.length === 0;
  }

  sendMessage() {
    if (this.newMessage.trim() === '') return;

    this.messages.push({ role: 'user', content: this.newMessage });
    this.cdr.detectChanges();
    this.scrollToBottom();

    const userMessage = this.messages.slice(-10);
    this.newMessage = '';
    this.isLoading = true;
    this.cdr.detectChanges();

    this.generateAIResponse(userMessage).subscribe({
      next: (response: string) => {
        this.messages.push({ role: 'assistant', content: response });
        this.isLoading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: (error) => {
        this.messages.push({ role: 'assistant', content: 'Có lỗi xảy ra.' });
        this.isLoading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      }
    });
  }

  generateAIResponse(message: Message[]): Observable<string> {
    return this.assistantService.getOutPut(message);
  }

  private scrollToBottom(): void {
    this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
  }

  @HostListener('keydown.tab', ['$event'])
  onTab(event: KeyboardEvent) {
    if (this.suggestion.trim()) {
      event.preventDefault();
      this.newMessage += (this.newMessage[this.newMessage.length - 1] === ' ') ? this.suggestion : ' ' + this.suggestion;
      this.suggestion = '';
      this.inputSubject.next(this.newMessage);
      setTimeout(() => this.inputBox.nativeElement.focus(), 0);
    }
  }

  onInput() {
    this.suggestion = '';
    this.inputSubject.next(this.newMessage);
  }

  selectSuggestion(question: string) {
    this.newMessage = question;
    this.sendMessage();
    this.showSuggestions = false;
  }

  getCursorPosition(): number {
    const input = this.inputBox.nativeElement;
    // Tạo một span tạm để đo độ rộng của text
    const span = document.createElement('span');
    span.style.visibility = 'hidden';
    span.style.position = 'absolute';
    span.style.whiteSpace = 'pre';
    span.style.font = window.getComputedStyle(input).font;
    span.textContent = this.newMessage;
    document.body.appendChild(span);
    const width = span.getBoundingClientRect().width;
    document.body.removeChild(span);
    
    // Thêm padding left của textarea
    return width + 16; // 16px là giá trị của pl-4 trong Tailwind
  }
}