import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html'
})
export class LoginComponent {

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  error: string | null = null;

  form = this.fb.group({
    correo: ['', [Validators.required, Validators.email]]
  });

  submit() {
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const correo = this.form.value.correo!;

    this.auth.login(correo).subscribe({
      next: user => {
        this.auth.saveSession(user);
        this.router.navigate(['/students']);
      },
      error: () => {
        this.error = 'Correo no válido';
      }
    });
  }
}