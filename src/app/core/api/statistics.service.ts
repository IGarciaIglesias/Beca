import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Statistics } from './statistics.model';

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private readonly API_URL = '/api/students/statistics';

  constructor(private http: HttpClient) {}

  getStatistics(): Observable<Statistics> {
    return this.http.get<Statistics>(this.API_URL);
  }
}