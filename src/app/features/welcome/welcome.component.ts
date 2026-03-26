import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule], 
  templateUrl: './welcome.component.html'
})
export class WelcomeComponent {

  constructor(
    public auth: AuthService,
    private router: Router
  ) {}

  go(path: string) {
    this.router.navigate([path]);
  }
}