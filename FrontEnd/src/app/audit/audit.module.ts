import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditComponent } from './audit/audit.component';
import {FormsModule} from "@angular/forms";
import {AuditCommonModule} from "eds-audittrail/dist/auditTrail";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    AuditCommonModule
  ],
  declarations: [AuditComponent]
})
export class AuditModule { }
