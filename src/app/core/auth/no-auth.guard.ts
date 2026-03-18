import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class NoAuthGuard implements CanActivate {

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (this.auth.isLoggedIn()) {
      // Ya hay sesión → fuera del login
      if (this.auth.isAdmin()) {
        this.router.navigate(['/students']);
      } else {
        this.router.navigate(['/welcome']);
      }
      return false;
    }
    return true;
  }
}