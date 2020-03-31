import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CohortDialogComponent } from './cohort-dialog.component';

describe('CohortDialogComponent', () => {
  let component: CohortDialogComponent;
  let fixture: ComponentFixture<CohortDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CohortDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CohortDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
