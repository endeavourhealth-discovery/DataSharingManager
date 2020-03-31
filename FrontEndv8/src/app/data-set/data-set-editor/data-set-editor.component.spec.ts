import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSetEditorComponent } from './data-set-editor.component';

describe('DataSetEditorComponent', () => {
  let component: DataSetEditorComponent;
  let fixture: ComponentFixture<DataSetEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSetEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSetEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
