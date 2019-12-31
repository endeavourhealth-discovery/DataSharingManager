import {NgModule} from '@angular/core';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from '@angular/platform-browser';
import {CommonModule} from '@angular/common';
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

//import {DataFlowComponent} from './data-flow/data-flow.component';
//import {DataFlowEditorComponent} from './data-flow-editor/data-flow-editor.component';
//import {DataFlowPickerComponent} from './data-flow-picker/data-flow-picker.component';
import {DataFlowService} from "./data-flow.service";

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
    //DataFlowComponent,
    //DataFlowEditorComponent,
    //DataFlowPickerComponent
  ],
  entryComponents : [
    //DataFlowPickerComponent
    //TODO remove temp code -start
    SchedulerComponent,
    //TODO remove temp code -end
  ],
  providers: [
    DataFlowService,
  ]
})
export class DataFlowModule { }
