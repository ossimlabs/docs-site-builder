import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessDeniedComponent } from './access-denied.component';
import { MatCardModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';

describe('AccessDeniedComponentComponent', () => {
  let component: AccessDeniedComponent;
  let fixture: ComponentFixture<AccessDeniedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessDeniedComponent ],
      imports: [
        MatCardModule,
        RouterTestingModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessDeniedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
