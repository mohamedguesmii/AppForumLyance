import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoriqueProcessusComponent } from './historique-processus.component';

describe('HistoriqueProcessusComponent', () => {
  let component: HistoriqueProcessusComponent;
  let fixture: ComponentFixture<HistoriqueProcessusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HistoriqueProcessusComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoriqueProcessusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
