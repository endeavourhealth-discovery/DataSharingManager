import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import {FormsModule} from "@angular/forms";
import {EntityViewComponentsModule} from "eds-angular4/dist/entityViewer";
import {ToastModule} from "ng2-toastr";
import {DialogsModule, LoggerService} from "eds-angular4";
import {ControlsModule} from "eds-angular4/dist/controls";
import {SchedulerService} from "./scheduler.service";
import {ModuleStateService} from "eds-angular4/dist/common";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    EntityViewComponentsModule,
    NgbModule,
    ToastModule.forRoot(),
    DialogsModule,
    ControlsModule
  ],
  declarations: [],
  providers: [SchedulerService, LoggerService, ModuleStateService]
})
export class SchedulerModule { }
