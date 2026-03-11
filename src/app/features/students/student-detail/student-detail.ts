import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { StudentService } from '../../../core/api/student.service';
import { Student } from '../../../core/api/student.model';

type Vm = {
  student: Student | null;
  loading: boolean;
  error: string | null;
};

@Component({
  selector: 'app-student-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './student-detail.html',
  styleUrls: ['./student-detail.css'] // opcional si ya lo tienes
})
export class StudentDetailComponent {
  private route = inject(ActivatedRoute);
  private api = inject(StudentService);

  private refresh$ = new BehaviorSubject<void>(undefined);

  private id$ = this.route.paramMap.pipe(
    map(pm => Number(pm.get('id'))),
    filter(id => Number.isFinite(id)),
    distinctUntilChanged()
  );

  vm$: Observable<Vm> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) =>
      this.api.get(id).pipe(
        map(student => ({ student, loading: false, error: null } as Vm)),
        startWith({ student: null, loading: true, error: null } as Vm),
        catchError((err) => {
          console.error('HTTP error', err);
          return of({ student: null, loading: false, error: 'No encontrado (404) o borrado' } as Vm);
        })
      )
    ),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  restore(studentId: number) {
    this.api.patch(studentId, { deleted: false }).subscribe({
      next: () => this.refresh$.next()
    });
  }
}