import { Routes } from '@angular/router';
import { StudentsListComponent } from './features/students/students-list/students-list';
import { StudentFormComponent } from './features/students/student-form/student-form';
import { StudentDetailComponent } from './features/students/student-detail/student-detail';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'students' },
  { path: 'students', component: StudentsListComponent },
  { path: 'students/new', component: StudentFormComponent },
  { path: 'students/:id', component: StudentDetailComponent },
  { path: 'students/:id/edit', component: StudentFormComponent },
  { path: '**', redirectTo: 'students' },
];