import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RagSearchComponent } from './rag-search.component';

describe('RagSearchComponent', () => {
  let component: RagSearchComponent;
  let fixture: ComponentFixture<RagSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RagSearchComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RagSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
