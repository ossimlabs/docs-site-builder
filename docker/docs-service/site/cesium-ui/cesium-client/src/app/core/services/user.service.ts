import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({providedIn: 'root'})
export class UserService {
    constructor() {}

    private userId = new BehaviorSubject('');

    currentUserId = this.userId.asObservable();

    private sessionId = new BehaviorSubject('');

    currentSessionId = this.sessionId.asObservable();

    setUserId(userId: string) {
        this.userId.next(userId);
    }

    setSessionId(sessionId: string) {
        this.sessionId.next(sessionId);
    }
}
