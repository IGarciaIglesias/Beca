import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { tap, catchError} from "rxjs/operators";
import { Observable } from "rxjs";

export interface Estudiante {
  id: number;
  nombre: string;
  edad: number;
  correo: string;
  deleted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class EstudianteService {
  private apiUrl = 'http://localhost:8080/api/estudiantes';
  constructor(private http: HttpClient) {}

  getEstudiantes(): Observable<Estudiante[]> {
    return this.http.get<Estudiante[]>(this.apiUrl).pipe();
  }

  createEstudiante(estudiante: Estudiante): Observable<Estudiante[]> {
    return this.http.post<Estudiante[]>(`${this.apiUrl}/crear`, estudiante).pipe();
  }

  updateEstudiante(estudiante: Estudiante): Observable<Estudiante[]> {
    return this.http.put<Estudiante[]>(`${this.apiUrl}/completo/${estudiante.id}`, estudiante);
  }

  deleteEstudiante(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/borradoHard/${id}`);
  }
}