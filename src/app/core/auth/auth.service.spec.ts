import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { AuthService } from './auth.service';
import { Student } from '../api/student.model';

describe('AuthService', () => {
  let service: AuthService;
  let router: Router;

  const admin: Student = { id: 1, name: 'Admin', age: 30, correo: 'admin@test.com', role: 'ADMIN' };
  const user: Student = { id: 2, name: 'User', age: 25, correo: 'user@test.com', role: 'USER' };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
    });
    service = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('starts logged out when there is no stored session', () => {
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.getUser()).toBeUndefined();
    expect(service.isAdmin()).toBeFalse();
  });

  it('saveSession stores user and marks as logged in', () => {
    service.saveSession(user);
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.getUser()?.correo).toBe('user@test.com');
    expect(localStorage.getItem('auth_user')).toContain('user@test.com');
  });

  it('isAdmin returns true only for ADMIN', () => {
    service.saveSession(admin);
    expect(service.isAdmin()).toBeTrue();

    service.saveSession(user);
    expect(service.isAdmin()).toBeFalse();
  });

  it('logout clears session and navigates to /login', () => {
    spyOn(router, 'navigate');
    service.saveSession(admin);

    service.logout();

    expect(service.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('auth_user')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

    it('loads existing session from localStorage in constructor', () => {
    localStorage.setItem('auth_user', JSON.stringify(admin));

    // IMPORTANT: forzar nueva instancia (constructor otra vez)
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
        imports: [RouterTestingModule],
    });

    const fresh = TestBed.inject(AuthService);

    expect(fresh.isLoggedIn()).toBeTrue();
    expect(fresh.getUser()?.role).toBe('ADMIN');
    });
});