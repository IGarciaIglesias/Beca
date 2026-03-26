import { Routes } from '@angular/router';

import { StudentsListComponent } from './features/students/students-list/students-list';
import { StudentFormComponent } from './features/students/student-form/student-form';
import { StudentDetailComponent } from './features/students/student-detail/student-detail';

import { ProfesoresListComponent } from './features/profesores/profesor-list/profesor-list';
import { ProfesorFormComponent } from './features/profesores/profesor-form/profesor-form';
import { ProfesorDetailComponent } from './features/profesores/profesor-detail/profesor-detail';

import { UsersListComponent } from './features/users/users-list/users-list';
import { UsersFormComponent } from './features/users/users-form/users-form';
import { UsersDetailComponent } from './features/users/users-detail/users-detail';

import { LoginComponent } from './features/auth/login/login.component';
import { WelcomeComponent } from './features/welcome/welcome.component';
import { StatisticsComponent } from './features/statistics/statistics.component';

import { NoAuthGuard } from './core/auth/no-auth.guard';
import { AuthGuard } from './core/auth/auth.guard';
import { AdminGuard } from './core/auth/admin.guard';

export const routes: Routes = [

  // ---------- PUBLIC ----------
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent, canActivate: [NoAuthGuard] },

  // ---------- HOME ----------
  { path: 'welcome', component: WelcomeComponent, canActivate: [AuthGuard] },

  // ---------- STATISTICS ----------
  { path: 'statistics', component: StatisticsComponent, canActivate: [AuthGuard] },

  // ---------- STUDENTS ----------
  // USER: ver
  // GESTOR / ADMIN: CRUD (controlado en componentes)
  { path: 'students', component: StudentsListComponent, canActivate: [AuthGuard] },
  { path: 'students/new', component: StudentFormComponent, canActivate: [AuthGuard] },
  { path: 'students/:id', component: StudentDetailComponent, canActivate: [AuthGuard] },
  { path: 'students/:id/edit', component: StudentFormComponent, canActivate: [AuthGuard] },

  // ---------- PROFESORES ----------
  // USER: ver
  // GESTOR / ADMIN: CRUD
  { path: 'profesores', component: ProfesoresListComponent, canActivate: [AuthGuard] },
  { path: 'profesores/new', component: ProfesorFormComponent, canActivate: [AuthGuard] },
  { path: 'profesores/:id', component: ProfesorDetailComponent, canActivate: [AuthGuard] },
  { path: 'profesores/:id/edit', component: ProfesorFormComponent, canActivate: [AuthGuard] },

  // ---------- USERS ----------
  // SOLO ADMIN
  { path: 'users', component: UsersListComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'users/new', component: UsersFormComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'users/:id', component: UsersDetailComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'users/:id/edit', component: UsersFormComponent, canActivate: [AuthGuard, AdminGuard] },

  // ---------- FALLBACK ----------
  { path: '**', redirectTo: 'login' }
];
