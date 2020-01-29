import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExtractDetailsDialogComponent } from './extract-details-dialog.component';

describe('ExtractDetailsDialogComponent', () => {
  let component: ExtractDetailsDialogComponent;
  let fixture: ComponentFixture<ExtractDetailsDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExtractDetailsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExtractDetailsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
