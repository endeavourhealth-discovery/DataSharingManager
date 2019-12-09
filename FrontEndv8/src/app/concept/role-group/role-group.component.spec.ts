import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RoleGroupComponent } from './role-group.component';

describe('RoleGroupComponent', () => {
  let component: RoleGroupComponent;
  let fixture: ComponentFixture<RoleGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RoleGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RoleGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
