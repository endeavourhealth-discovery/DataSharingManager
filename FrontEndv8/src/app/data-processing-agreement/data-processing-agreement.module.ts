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
import {DocumentationService} from "../documentation/documentation.service";

//import {DataProcessingAgreementComponent} from './data-processing-agreement/data-processing-agreement.component';
//import {DataProcessingAgreementEditorComponent} from "./data-processing-agreement-editor/data-processing-agreement-editor.component";
//import {DataProcessingAgreementPickerComponent} from './data-processing-agreement-picker/data-processing-agreement-picker.component';
import {DataProcessingAgreementService} from "./data-processing-agreement.service";

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
    //DataProcessingAgreementComponent,
    //DataProcessingAgreementEditorComponent,
    //DataProcessingAgreementPickerComponent
  ],
  entryComponents : [
    //DataProcessingAgreementPickerComponent
    //TODO remove temp code -start
    SchedulerComponent,
    //TODO remove temp code -end
  ],
  providers: [
    DataProcessingAgreementService,
    DocumentationService
  ]
})
export class DataProcessingAgreementModule { }
