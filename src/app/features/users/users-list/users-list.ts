import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';

import { UserService } from '../../../core/api/user.service';
import { User } from '../../../core/api/user.model';
import { AuthService } from '../../../core/auth/auth.service';

type Vm = {
  users: User[];
  loading: boolean;
  error: string | null;
};

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './users-list.html',
  styleUrls: ['./users-list.css']
})
export class UsersListComponent {

  private refresh$ = new BehaviorSubject<void>(undefined);

  vm$: Observable<Vm> = this.refresh$.pipe(
    switchMap(() =>
      this.api.list().pipe(
        map(users => ({ users, loading: false, error: null })),
        startWith({ users: [], loading: true, error: null }),
        catchError(err => {
          console.error('HTTP error', err);
          return of({
            users: [],
            loading: false,
            error: 'Error cargando usuarios'
          });
        })
      )
    )
  );

  constructor(
    private api: UserService,
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