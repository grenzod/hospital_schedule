import { fromEvent, map, Observable, Subject } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { Message } from "../Components/assistant/assistant.component";

export interface Suggestion {
    token: string;
    full_text: string;
}

@Injectable({
    providedIn: 'root'
})
export class AssistantService {
    private api = `${environment.apiAssistantUrl}`;
    private ws!: WebSocket;
    private suggestions$ = new Subject<Suggestion>();

    constructor(private http: HttpClient) { }

    getOutPut(query: Message[]): Observable<string> {
        return this.http.post<string>(this.api + '/hospital-rag-agent', query);
    }

    connect() {
        this.ws = new WebSocket('ws://localhost:8000/ws/predict');
        fromEvent<MessageEvent>(this.ws, 'message')
            .pipe(
                map(evt => JSON.parse(evt.data) as Suggestion)
            )
            .subscribe(this.suggestions$);
    }

    send(text: string) {
        if (this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(text);
        }
    }

    onSuggestion(): Observable<Suggestion> {
        return this.suggestions$.asObservable();
    }
}
