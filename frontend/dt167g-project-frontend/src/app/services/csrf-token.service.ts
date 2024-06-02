import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CsrfTokenService {
  private csrfTokenSubject = new BehaviorSubject<string | null>(null);

  constructor() { }

  setCsrfToken(token: string): void {
    this.csrfTokenSubject.next(token);
  }

  getCsrfToken(): string | null {
    return this.csrfTokenSubject.value;
  }

}
