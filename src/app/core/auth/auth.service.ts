import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Student } from '../api/student.model';

const STORAGE_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private currentUser?: Student;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      this.currentUser = JSON.parse(stored);
    }
  }

  login(correo: string) {
    return this.http.post<Student>('/api/auth/login', { correo });
  }

  saveSession(user: Student) {
    this.currentUser = user;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  }

  logout() {
    this.currentUser = undefined;
    localStorage.removeItem(STORAGE_KEY);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.currentUser;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  getUser(): Student | undefined {
    return this.currentUser;
  }
}