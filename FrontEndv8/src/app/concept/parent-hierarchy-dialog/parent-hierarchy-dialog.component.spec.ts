import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ParentHierarchyDialogComponent } from './parent-hierarchy-dialog.component';

describe('ParentHierarchyDialogComponent', () => {
  let component: ParentHierarchyDialogComponent;
  let fixture: ComponentFixture<ParentHierarchyDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ParentHierarchyDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParentHierarchyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
