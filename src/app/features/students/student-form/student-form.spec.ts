import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, ActivatedRoute, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { StudentFormComponent } from './student-form';
import { StudentService } from '../../../core/api/student.service';

describe('StudentFormComponent', () => {
  let fixture: ComponentFixture<StudentFormComponent>;
  let component: StudentFormComponent;
  let router: Router;

  const api = jasmine.createSpyObj<StudentService>('StudentService', ['get', 'create', 'replace']);

  beforeEach(async () => {
    api.get.and.returnValue(of({ id: 1, name: 'Pepe', age: 20, correo: 'pepe@test.com', role: 'USER' } as any));
    api.create.and.returnValue(of({ id: 99, name: 'X', age: 1, correo: 'x@x.com', role: 'USER' } as any));
    api.replace.and.returnValue(of({ id: 1, name: 'Y', age: 2, correo: 'y@y.com', role: 'ADMIN' } as any));

    await TestBed.configureTestingModule({
      imports: [StudentFormComponent, RouterTestingModule],
      providers: [
        { provide: StudentService, useValue: api },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({}) } }, // modo new
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StudentFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);

    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('submit() invalid should mark touched and NOT call api', () => {
    component.form.patchValue({ name: '', age: null, correo: '', role: 'USER' });
    component.submit();

    expect(api.create).not.toHaveBeenCalled();
    expect(api.replace).not.toHaveBeenCalled();
  });

  it('submit() in create mode should call api.create', () => {
    component.form.patchValue({ name: 'A', age: 1, correo: 'a@a.com', role: 'USER' });
    component.submit();

    expect(api.create).toHaveBeenCalled();
  });
});