import { NgModule } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import { OrganisationComponent } from './organisation/organisation.component';
import { OrganisationService } from './organisation.service';
import {
  MatButtonModule,
  MatCardModule, MatCheckboxModule, MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule, MatMenuModule,
  MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule, MatSelectModule, MatSnackBarModule,
  MatSortModule,
  MatTableModule, MatTreeModule
} from '@angular/material';
import {FlexModule} from "@angular/flex-layout";
import {RouterModule} from "@angular/router";
import {CoreModule} from "dds-angular8";
import {GenericTableComponent} from "../generic-table/generic-table.component";
import {GenericTableSspComponent} from "../generic-table-ssp/generic-table-ssp.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";

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
    MatProgressBarModule
  ],
  declarations: [
    OrganisationComponent,
    GenericTableComponent,
    GenericTableSspComponent
  ],
  entryComponents : [
  ],
  providers: [
    OrganisationService,
    DatePipe]
})
export class OrganisationModule { }
