import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchingForumsComponent } from './matching-forums.component';

describe('MatchingForumsComponent', () => {
  let component: MatchingForumsComponent;
  let fixture: ComponentFixture<MatchingForumsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MatchingForumsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MatchingForumsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
