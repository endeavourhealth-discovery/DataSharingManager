import { NgModule } from '@angular/core';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule, DatePipe} from '@angular/common';
import {CoreModule} from "dds-angular8";
import {FlexModule} from "@angular/flex-layout";
import {FormsModule} from '@angular/forms';
import {
  MatBadgeModule,
  MatButtonModule,
  MatCardModule, MatCheckboxModule, MatDialogModule, MatDividerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule, MatMenuModule,
  MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule, MatSelectModule, MatSnackBarModule,
  MatSortModule,
  MatTableModule, MatTabsModule, MatTreeModule
} from '@angular/material';
import {RouterModule} from "@angular/router";

import {OrganisationComponent} from './organisation/organisation.component';
import {OrganisationService} from './organisation.service';
import {OrganisationEditorComponent} from "./organisation-editor/organisation-editor.component";
import {OrganisationPickerComponent} from './organisation-picker/organisation-picker.component';
import {GenericTableModule} from "../generic-table/generic-table.module";
//TODO remove temp code -start
import {SchedulerComponent} from "../scheduler/scheduler/scheduler.component";
import {DataSetPickerComponent} from "../data-set/data-set-picker/data-set-picker.component";
import {CohortPickerComponent} from "../cohort/cohort-picker/cohort-picker.component";
//TODO remove temp code -end

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    FormsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    RouterModule,
    FlexModule,
    MatSelectModule,
    MatSnackBarModule,
    MatCheckboxModule,
    MatMenuModule,
    MatDialogModule,
    CoreModule,
    MatButtonModule,
    MatTreeModule,
    MatProgressBarModule,
    MatDividerModule,
    MatTabsModule,
    MatBadgeModule,
    GenericTableModule
  ],
  declarations: [
    OrganisationComponent,
    OrganisationEditorComponent,
    OrganisationPickerComponent
  ],
  entryComponents : [
    //TODO remove temp code -start
    SchedulerComponent,
    OrganisationPickerComponent,
    DataSetPickerComponent,
    CohortPickerComponent,
    //TODO remove temp code -end
  ],
  providers: [
    OrganisationService,
    DatePipe]
})
export class OrganisationModule { }
