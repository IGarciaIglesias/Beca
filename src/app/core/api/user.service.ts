import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private base = '/api/users'; // proxy => http://localhost:8080/users

  constructor(private http: HttpClient) {}

  list(): Observable<User[]> {
    return this.http.get<User[]>(this.base);
  }

  get(id: number): Observable<User> {
    return this.http.get<User>(`${this.base}/${id}`);
  }

  create(u: User): Observable<User> {
    return this.http.post<User>(this.base, u);
  }

  replace(id: number, u: User): Observable<User> {
    return this.http.put<User>(`${this.base}/${id}`, u);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
``