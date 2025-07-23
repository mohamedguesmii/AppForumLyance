import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RendezzVousComponent } from './rendezz-vous.component';

describe('RendezzVousComponent', () => {
  let component: RendezzVousComponent;
  let fixture: ComponentFixture<RendezzVousComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RendezzVousComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RendezzVousComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
