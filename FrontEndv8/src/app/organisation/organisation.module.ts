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
import {GenericTableSspComponent} from "../generic-table/generic-table-ssp/generic-table-ssp.component";
import {GenericTableComponent} from "../generic-table/generic-table/generic-table.component";
import {OrganisationEditorComponent} from "./organisation-editor/organisation-editor.component";

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
    MatBadgeModule
  ],
  declarations: [
    OrganisationComponent,
    OrganisationEditorComponent,
    GenericTableSspComponent,
    GenericTableComponent
  ],
  entryComponents : [
  ],
  providers: [
    OrganisationService,
    DatePipe]
})
export class OrganisationModule { }
