import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { StudentDetailComponent } from './student-detail';
import { StudentService } from '../../../core/api/student.service';

describe('StudentDetailComponent', () => {
  let fixture: ComponentFixture<StudentDetailComponent>;
  let component: StudentDetailComponent;

  const api = jasmine.createSpyObj<StudentService>('StudentService', ['get', 'patch']);

  beforeEach(async () => {
    api.get.and.returnValue(of({ id: 1, name: 'Ana', age: 22, correo: 'ana@test.com', role: 'ADMIN' } as any));
    api.patch.and.returnValue(of({ id: 1, name: 'Ana', age: 22, correo: 'ana@test.com', role: 'ADMIN' } as any));

    await TestBed.configureTestingModule({
      imports: [StudentDetailComponent, RouterTestingModule],
      providers: [
        { provide: StudentService, useValue: api },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: '1' })),       
            snapshot: { paramMap: convertToParamMap({ id: '1' }) }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StudentDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load student by id', () => {
    expect(component).toBeTruthy();
    expect(api.get).toHaveBeenCalled();
  });
});