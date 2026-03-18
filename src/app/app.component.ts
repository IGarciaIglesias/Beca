import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet], // ✅ necesario para *ngIf y router-outlet
  templateUrl: './app.html'              // ✅ ESTE archivo
})
export class AppComponent {
  constructor(public auth: AuthService) {}
}