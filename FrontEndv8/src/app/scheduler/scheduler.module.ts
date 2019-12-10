import { NgModule } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import { SchedulerComponent } from './scheduler/scheduler.component';
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDialogModule} from "@angular/material/dialog";
import {MatTabsModule} from "@angular/material/tabs";
import {MatSelectModule} from "@angular/material/select";
import {MatInputModule} from "@angular/material";
import {SchedulerService} from "./scheduler.service";
import {MatRadioModule} from "@angular/material/radio";
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [SchedulerComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatGridListModule,
    MatFormFieldModule,
    MatDialogModule,
    MatTabsModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    FormsModule
  ],
  providers: [
    SchedulerService
  ]
})
export class SchedulerModule { }
