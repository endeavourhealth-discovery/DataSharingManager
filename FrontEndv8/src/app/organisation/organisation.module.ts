import { NgModule } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import { OrganisationComponent } from './organisation/organisation.component';
import { OrganisationService } from './organisation.service';
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
import {FlexModule} from "@angular/flex-layout";
import {RouterModule} from "@angular/router";
import {CoreModule} from "dds-angular8";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {OrganisationEditorComponent} from "./organisation-editor/organisation-editor.component";
//TODO remove temp code -start
import {SchedulerComponent} from "../scheduler/scheduler/scheduler.component";
import {GenericTableModule} from "../generic-table/generic-table.module";
import { OrganisationPickerComponent } from './organisation-picker/organisation-picker.component';
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
    OrganisationPickerComponent
    //TODO remove temp code -end
  ],
  providers: [
    OrganisationService,
    DatePipe]
})
export class OrganisationModule { }
