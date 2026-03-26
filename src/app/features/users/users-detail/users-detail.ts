import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { UserService } from '../../../core/api/user.service';
import { User } from '../../../core/api/user.model';

type Vm = {
  user: User | null;
  loading: boolean;
  error: string | null;
};

@Component({
  selector: 'app-users-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './users-detail.html'
})
export class UsersDetailComponent {

  private route = inject(ActivatedRoute);
  private api = inject(UserService);
  private refresh$ = new BehaviorSubject<void>(undefined);

  private id$ = this.route.paramMap.pipe(
    map(pm => Number(pm.get('id'))),
    filter(id => Number.isFinite(id)),
    distinctUntilChanged()
  );

  vm$: Observable<Vm> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) =>
      this.api.get(id).pipe(
        map(user => ({ user, loading: false, error: null })),
        startWith({ user: null, loading: true, error: null }),
        catchError(err => {
          console.error('HTTP error', err);
          return of({
            user: null,
            loading: false,
            error: 'Usuario no encontrado'
          });
        })
      )
    ),
    shareReplay({ bufferSize: 1, refCount: true })
  );
}
