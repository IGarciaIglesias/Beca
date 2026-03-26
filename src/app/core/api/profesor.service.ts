import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Profesor } from './profesor.model';

@Injectable({ providedIn: 'root' })
export class ProfesorService {

  private base = '/api/profesores'; // proxy => http://localhost:8080/profesores

  constructor(private http: HttpClient) {}

  list(): Observable<Profesor[]> {
    return this.http.get<Profesor[]>(this.base);
  }

  get(id: number): Observable<Profesor> {
    return this.http.get<Profesor>(`${this.base}/${id}`);
  }

  create(p: Profesor): Observable<Profesor> {
    return this.http.post<Profesor>(this.base, p);
  }

  replace(id: number, p: Profesor): Observable<Profesor> {
    return this.http.put<Profesor>(`${this.base}/${id}`, p);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
