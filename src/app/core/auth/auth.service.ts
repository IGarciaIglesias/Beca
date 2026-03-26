import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { User } from '../api/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly STORAGE_KEY = 'auth_user';

  private userSubject = new BehaviorSubject<User | null>(this.loadUser());
  user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(correo: string) {
    return this.http.post<User>('http://localhost:8080/auth/login', { correo })
      .pipe(
        tap(user => {
          localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
          this.userSubject.next(user);
        })
      );
  }

  logout() {
    localStorage.removeItem(this.STORAGE_KEY);
    this.userSubject.next(null);
  }

  get user(): User | null {
    return this.userSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.user;
  }

  isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  hasRole(...roles: string[]): boolean {
    return !!this.user && roles.includes(this.user.role);
  }

  private loadUser(): User | null {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}