import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EntretienvisioComponent } from './entretienvisio.component';

describe('EntretienvisioComponent', () => {
  let component: EntretienvisioComponent;
  let fixture: ComponentFixture<EntretienvisioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EntretienvisioComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EntretienvisioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
