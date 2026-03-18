import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { AdminGuard } from './admin.guard';
import { AuthService } from './auth.service';

describe('AdminGuard', () => {
  let guard: AdminGuard;
  let auth: AuthService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [AdminGuard, AuthService],
    });

    guard = TestBed.inject(AdminGuard);
    auth = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('redirects to /welcome if user is not admin', () => {
    spyOn(router, 'navigate');
    auth.saveSession({ id: 2, name: 'U', correo: 'u@u.com', role: 'USER' } as any);

    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/welcome']);
  });

  it('allows navigation for admin', () => {
    spyOn(router, 'navigate');
    auth.saveSession({ id: 1, name: 'A', correo: 'a@a.com', role: 'ADMIN' } as any);

    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});