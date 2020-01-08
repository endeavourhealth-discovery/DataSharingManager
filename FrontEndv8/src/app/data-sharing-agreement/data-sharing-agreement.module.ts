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

import {DataSharingAgreementComponent} from './data-sharing-agreement/data-sharing-agreement.component';
import {DataSharingAgreementEditorComponent} from './data-sharing-agreement-editor/data-sharing-agreement-editor.component';
//import {DataSharingAgreementPickerComponent} from './data-sharing-agreement-picker/data-sharing-agreement-picker.component';
//import {PurposeAddComponent} from './purpose-add/purpose-add.component';
import {DataSharingAgreementService} from './data-sharing-agreement.service';
import {GenericTableModule} from '../generic-table/generic-table.module';

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
    DataSharingAgreementComponent,
    DataSharingAgreementEditorComponent,
    //DataSharingAgreementPickerComponent,
    //PurposeAddComponent
  ],
  entryComponents : [
    //DataSharingAgreementPickerComponent,
    //PurposeAddComponent
  ],
  providers: [
    DataSharingAgreementService,
    DatePipe
  ]
})
export class DataSharingAgreementModule { }