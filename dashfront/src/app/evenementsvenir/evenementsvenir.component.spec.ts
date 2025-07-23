import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvenementsvenirComponent } from './evenementsvenir.component';

describe('EvenementsvenirComponent', () => {
  let component: EvenementsvenirComponent;
  let fixture: ComponentFixture<EvenementsvenirComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EvenementsvenirComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvenementsvenirComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
