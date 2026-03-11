import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';
import { StudentService } from '../../../core/api/student.service';
import { Student } from '../../../core/api/student.model';

type Vm = {
  students: Student[];
  loading: boolean;
  error: string | null;
};

@Component({
  selector: 'app-students-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './students-list.html',
  styleUrls: ['./students-list.css']   
})
export class StudentsListComponent {
  private refresh$ = new BehaviorSubject<void>(undefined);

  vm$: Observable<Vm> = this.refresh$.pipe(
    switchMap(() =>
      this.api.list().pipe(
        map((students) => ({ students, loading: false, error: null } as Vm)),
        startWith({ students: [], loading: true, error: null } as Vm),
        catchError((err) => {
          console.error('HTTP error', err);
          return of({ students: [], loading: false, error: 'Error cargando alumnos' } as Vm);
        })
      )
    )
  );

  constructor(private api: StudentService) {}

  reload() {
    this.refresh$.next();
  }

  deleteSoft(id: number) {
    this.api.delete(id, true).subscribe({ next: () => this.reload() });
  }

  deleteHard(id: number) {
    this.api.delete(id, false).subscribe({ next: () => this.reload() });
  }

  refreshCache() {
    this.api.refreshCache().subscribe({ next: () => this.reload() });
  }
}