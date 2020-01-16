import {NgModule} from '@angular/core';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from '@angular/platform-browser';
import {CommonModule} from '@angular/common';
import {CoreModule, GenericTableModule, MessageBoxDialogComponent} from 'dds-angular8';
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

import {DataProcessingAgreementComponent} from './data-processing-agreement/data-processing-agreement.component';
import {DataProcessingAgreementEditorComponent} from "./data-processing-agreement-editor/data-processing-agreement-editor.component";
import {DataProcessingAgreementService} from "./data-processing-agreement.service";
import {PurposeComponent} from "../purpose/purpose/purpose.component";
import {DocumentationComponent} from "../documentation/documentation/documentation.component";
import { DataProcessingAgreementPickerComponent } from './data-processing-agreement-picker/data-processing-agreement-picker.component';

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
    DataProcessingAgreementComponent,
    DataProcessingAgreementEditorComponent,
    DataProcessingAgreementPickerComponent,
    //DataProcessingAgreementPickerComponent
  ],
  entryComponents : [
    //DataProcessingAgreementPickerComponent
    PurposeComponent,
    DocumentationComponent,
  ],
  providers: [
    DataProcessingAgreementService,
    DocumentationService,
  ]
})
export class DataProcessingAgreementModule {
}
