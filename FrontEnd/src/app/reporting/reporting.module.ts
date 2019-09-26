import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportsComponent } from './reports/reports.component';
import {DialogsModule, LoggerService} from "eds-angular4";
import {ReportingService} from "./reporting.service";
import {ToastModule} from "ng2-toastr";
import {ControlsModule} from "eds-angular4/dist/controls";
import {FormsModule} from "@angular/forms";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ToastModule.forRoot(),
    NgbModule,
    DialogsModule,
    ControlsModule
  ],
  declarations: [ReportsComponent],
  providers: [
    ReportingService,
    LoggerService
  ]
})
export class ReportingModule { }
