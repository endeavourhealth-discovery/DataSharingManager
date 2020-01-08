import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSharingAgreementEditorComponent } from './data-sharing-agreement-editor.component';

describe('DataSharingAgreementEditorComponent', () => {
  let component: DataSharingAgreementEditorComponent;
  let fixture: ComponentFixture<DataSharingAgreementEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataSharingAgreementEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSharingAgreementEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
