import { NgModule } from '@angular/core';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule, DatePipe} from '@angular/common';
import {CoreModule, GenericTableModule, ItemLinkageModule} from "dds-angular8";
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
import { OrganisationDialogComponent } from './organisation-dialog/organisation-dialog.component';
import { AddressDialogComponent } from './address-dialog/address-dialog.component';

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
    GenericTableModule,
    ItemLinkageModule
  ],
  declarations: [
    OrganisationComponent,
    OrganisationEditorComponent,
    OrganisationPickerComponent,
    OrganisationDialogComponent,
    AddressDialogComponent
  ],
  entryComponents : [
    OrganisationPickerComponent,
    OrganisationDialogComponent,
    AddressDialogComponent
  ],
  providers: [
    OrganisationService,
    DatePipe]
})
export class OrganisationModule { }
