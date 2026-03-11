import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Student {
  id?: number;
  name: string;
  age: number;
  correo: string;
  deleted?: boolean;
}

@Injectable({ providedIn: 'root' })
export class StudentService {
  private base = '/api/students'; // IMPORTANTE: proxy => http://localhost:8080/students

  //private base = 'http://localhost:8080/students';

  constructor(private http: HttpClient) {}

  list(): Observable<Student[]> {
    return this.http.get<Student[]>(this.base);
  }

  get(id: number): Observable<Student> {
    return this.http.get<Student>(`${this.base}/${id}`);
  }

  create(s: Student): Observable<Student> {
    return this.http.post<Student>(this.base, s);
  }

  replace(id: number, s: Student): Observable<Student> {
    return this.http.put<Student>(`${this.base}/${id}`, s);
  }

  patch(id: number, fields: Partial<Student>): Observable<Student> {
    return this.http.patch<Student>(`${this.base}/${id}`, fields);
  }

  delete(id: number, soft = true): Observable<void> {
    const params = new HttpParams().set('soft', String(soft));
    return this.http.delete<void>(`${this.base}/${id}`, { params });
  }

  refreshCache(): Observable<{ status: string; cleared: string[] }> {
    return this.http.get<{ status: string; cleared: string[] }>(`${this.base}/refresh`);
  }
}