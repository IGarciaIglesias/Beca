import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { ProfesorService } from '../../../core/api/profesor.service';
import { Profesor } from '../../../core/api/profesor.model';

type Vm = {
  profesor: Profesor | null;
  loading: boolean;
  error: string | null;
};

@Component({
  selector: 'app-profesor-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profesor-detail.html',
    styleUrls: ['./profesor-detail.css']
})
export class ProfesorDetailComponent {

  private route = inject(ActivatedRoute);
  private api = inject(ProfesorService);
  private refresh$ = new BehaviorSubject<void>(undefined);

  private id$ = this.route.paramMap.pipe(
    map(pm => Number(pm.get('id'))),
    filter(id => Number.isFinite(id)),
    distinctUntilChanged()
  );

  vm$: Observable<Vm> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) =>
      this.api.get(id).pipe(
        map(profesor => ({ profesor, loading: false, error: null })),
        startWith({ profesor: null, loading: true, error: null }),
        catchError(err => {
          console.error('HTTP error', err);
          return of({
            profesor: null,
            loading: false,
            error: 'Profesor no encontrado'
          });
        })
      )
    ),
    shareReplay({ bufferSize: 1, refCount: true })
  );
}
``