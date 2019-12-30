import {NgModule} from '@angular/core';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from '@angular/platform-browser';
import {CommonModule, DatePipe} from '@angular/common';
import {CoreModule} from 'dds-angular8';
import {FlexModule} from '@angular/flex-layout';
import {FormsModule} from '@angular/forms';
import {
  MatBadgeModule, MatButtonModule,
  MatCardModule, MatCheckboxModule,
  MatDialogModule, MatDividerModule,
  MatFormFieldModule,
  MatIconModule, MatInputModule,
  MatMenuModule,
  MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule,
  MatSelectModule, MatSnackBarModule, MatSortModule,
  MatTableModule, MatTabsModule, MatTreeModule
} from '@angular/material';
import {RouterModule} from '@angular/router';

//import {CohortComponent} from './cohort/cohort.component';
//import {CohortEditorComponent} from "./cohort-editor/cohort-editor.component";
//import {CohortPickerComponent} from './cohort-picker/cohort-picker.component';
import {CohortService} from './cohort.service';

//TODO remove temp code -start
import {SchedulerComponent} from '../scheduler/scheduler/scheduler.component';
import {GenericTableModule} from '../generic-table/generic-table.module';
//TODO remove temp code -end

@NgModule({
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    CommonModule,
    CoreModule,
    FlexModule,
    FormsModule,
    GenericTableModule,
    MatBadgeModule, MatButtonModule,
    MatCardModule, MatCheckboxModule,
    MatDialogModule, MatDividerModule,
    MatFormFieldModule,
    MatIconModule, MatInputModule,
    MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule,
    MatSelectModule, MatSnackBarModule, MatSortModule,
    MatTableModule, MatTabsModule, MatTreeModule,
    RouterModule,
  ],
  declarations: [
    //CohortComponent,
    //CohortEditorComponent,
    //CohortPickerComponent
  ],
  entryComponents : [
    //CohortPickerComponent
    //TODO remove temp code -start
    SchedulerComponent,
    //TODO remove temp code -end
  ],
  providers: [
    CohortService,
  ]
})
export class CohortModule { }
