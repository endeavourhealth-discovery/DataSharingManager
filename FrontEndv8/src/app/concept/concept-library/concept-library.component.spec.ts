import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConceptLibraryComponent } from './concept-library.component';

describe('ConceptLibraryComponent', () => {
  let component: ConceptLibraryComponent;
  let fixture: ComponentFixture<ConceptLibraryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConceptLibraryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConceptLibraryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
