import { NgModule } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import { ProjectComponent } from './project/project.component';
import { ProjectPickerComponent } from './project-picker/project-picker.component';
import { ProjectEditorComponent } from './project-editor/project-editor.component';
import {FormsModule} from "@angular/forms";
import {DialogsModule, LoggerService} from "eds-angular4";
import {ToastModule} from "ng2-toastr";
import {ControlsModule} from "eds-angular4/dist/controls";
import {EntityViewComponentsModule} from "eds-angular4/dist/entityViewer";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ProjectService} from "./project.service";
import {SchedulerPickerComponent} from "../scheduler/scheduler-picker/scheduler-picker.component";
import {SchedulerService} from "../scheduler/scheduler.service";

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
  declarations: [
    ProjectComponent,
    ProjectPickerComponent,
    ProjectEditorComponent,
    SchedulerPickerComponent
  ],
  entryComponents: [
    ProjectPickerComponent,
    SchedulerPickerComponent
  ],
  providers: [
    ProjectService,
    LoggerService,
    DatePipe,
    SchedulerService
  ]
})
export class ProjectModule { }
