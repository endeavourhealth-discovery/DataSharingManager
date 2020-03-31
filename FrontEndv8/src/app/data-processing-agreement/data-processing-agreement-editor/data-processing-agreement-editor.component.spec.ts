import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataProcessingAgreementEditorComponent } from './data-processing-agreement-editor.component';

describe('DataProcessingAgreementEditorComponent', () => {
  let component: DataProcessingAgreementEditorComponent;
  let fixture: ComponentFixture<DataProcessingAgreementEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataProcessingAgreementEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataProcessingAgreementEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
