import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-welcome',
  imports: [CommonModule],
  template: `
    <h2>Bienvenido</h2>
    <p>Has iniciado sesión como usuario.</p>
  `
})
export class WelcomeComponent {}