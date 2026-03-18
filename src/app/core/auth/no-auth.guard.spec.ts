import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { NoAuthGuard } from './no-auth.guard';
import { AuthService } from './auth.service';

describe('NoAuthGuard', () => {
  let guard: NoAuthGuard;
  let auth: AuthService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [NoAuthGuard, AuthService],
    });

    guard = TestBed.inject(NoAuthGuard);
    auth = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('allows entering /login if NOT logged in', () => {
    spyOn(router, 'navigate');
    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('blocks /login and redirects admin to /students', () => {
    spyOn(router, 'navigate');
    auth.saveSession({ id: 1, name: 'A', correo: 'a@a.com', role: 'ADMIN' } as any);

    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/students']);
  });

  it('blocks /login and redirects user to /welcome', () => {
    spyOn(router, 'navigate');
    auth.saveSession({ id: 2, name: 'U', correo: 'u@u.com', role: 'USER' } as any);

    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/welcome']);
  });
});