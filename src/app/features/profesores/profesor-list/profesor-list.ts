import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';

import { ProfesorService } from '../../../core/api/profesor.service';
import { Profesor } from '../../../core/api/profesor.model';
import { AuthService } from '../../../core/auth/auth.service';

type Vm = {
  profesores: Profesor[];
  loading: boolean;
  error: string | null;
};

@Component({
  selector: 'app-profesores-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profesor-list.html',
  styleUrls: ['./profesor-list.css']
})
export class ProfesoresListComponent {

  private refresh$ = new BehaviorSubject<void>(undefined);

  vm$: Observable<Vm> = this.refresh$.pipe(
    switchMap(() =>
      this.api.list().pipe(
        map(profesores => ({ profesores, loading: false, error: null })),
        startWith({ profesores: [], loading: true, error: null }),
        catchError(err => {
          console.error('HTTP error', err);
          return of({
            profesores: [],
            loading: false,
            error: 'Error cargando profesores'
          });
        })
      )
    )
  );

  constructor(
    private api: ProfesorService,
    public auth: AuthService
  ) {}

  reload() {
    this.refresh$.next();
  }

  delete(id: number) {
    this.api.delete(id).subscribe({
      next: () => this.reload()
    });
  }
}