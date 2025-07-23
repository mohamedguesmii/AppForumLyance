import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessusDetailsComponent } from './processus-details.component';

describe('ProcessusDetailsComponent', () => {
  let component: ProcessusDetailsComponent;
  let fixture: ComponentFixture<ProcessusDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessusDetailsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProcessusDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
