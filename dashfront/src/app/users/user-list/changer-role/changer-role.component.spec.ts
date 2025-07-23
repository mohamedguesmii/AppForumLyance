import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangerRoleComponent } from './changer-role.component';

describe('ChangerRoleComponent', () => {
  let component: ChangerRoleComponent;
  let fixture: ComponentFixture<ChangerRoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChangerRoleComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChangerRoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
