import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {GenericTableSspComponent} from "./generic-table-ssp/generic-table-ssp.component";
import {GenericTableComponent} from "./generic-table/generic-table.component";
import {
  MatBadgeModule,
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDialogModule,
  MatDividerModule, MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule, MatPaginatorModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  MatSelectModule, MatSnackBarModule, MatSortModule, MatTableModule,
  MatTabsModule,
  MatTreeModule
} from "@angular/material";
import {BrowserModule} from "@angular/platform-browser";
import {RouterModule} from "@angular/router";
import {FlexModule} from "@angular/flex-layout";
import {FormsModule} from "@angular/forms";
import {CoreModule} from "dds-angular8";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";


@NgModule({
  declarations: [
  GenericTableSspComponent,
  GenericTableComponent
],
  exports: [
  GenericTableComponent,
  GenericTableSspComponent
],
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
  ]
})
export class GenericTableModule { }
