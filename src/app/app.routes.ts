import { Routes } from '@angular/router';

import { StudentsListComponent } from './features/students/students-list/students-list';
import { StudentFormComponent } from './features/students/student-form/student-form';
import { StudentDetailComponent } from './features/students/student-detail/student-detail';
import { LoginComponent } from './features/auth/login/login.component';
import { WelcomeComponent } from './features/welcome/welcome.component';
import { NoAuthGuard } from './core/auth/no-auth.guard';
import { AuthGuard } from './core/auth/auth.guard';
import { AdminGuard } from './core/auth/admin.guard';

export const routes: Routes = [

  { path: '', pathMatch: 'full', redirectTo: 'login' },

  { path: 'login', component: LoginComponent, canActivate: [NoAuthGuard] },

  { path: 'welcome', component: WelcomeComponent, canActivate: [AuthGuard] },

  // SOLO ADMIN
  { path: 'students', component: StudentsListComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'students/new', component: StudentFormComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'students/:id', component: StudentDetailComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'students/:id/edit', component: StudentFormComponent, canActivate: [AuthGuard, AdminGuard] },

  { path: '**', redirectTo: 'login' },
];