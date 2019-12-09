import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChildHierarchyDialogComponent } from './child-hierarchy-dialog.component';

describe('ParentHierarchyDialogComponent', () => {
  let component: ChildHierarchyDialogComponent;
  let fixture: ComponentFixture<ChildHierarchyDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ChildHierarchyDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChildHierarchyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
