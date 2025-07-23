import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkflowMonitoringComponent } from './workflow-monitoring.component';

describe('WorkflowMonitoringComponent', () => {
  let component: WorkflowMonitoringComponent;
  let fixture: ComponentFixture<WorkflowMonitoringComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WorkflowMonitoringComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkflowMonitoringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
