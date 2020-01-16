import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RegionComponent } from './region/region.component';
import {RegionService} from './region.service';
import { RegionEditorComponent } from './region-editor/region-editor.component';
import {
  MatBadgeModule,
  MatButtonModule, MatCardModule, MatCheckboxModule, MatDialogModule, MatDividerModule,
  MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule,
  MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule, MatSelectModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule, MatTabsModule, MatTreeModule
} from "@angular/material";
import {CoreModule, GenericTableModule} from "dds-angular8";
import {RouterModule} from "@angular/router";
import {FlexModule} from "@angular/flex-layout";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule} from "@angular/forms";
import {RegionPickerComponent} from "./region-picker/region-picker.component";
import {DataSharingAgreementPickerComponent} from "../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {DataProcessingAgreementPickerComponent} from "../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {OrganisationPickerComponent} from "../organisation/organisation-picker/organisation-picker.component";

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
    RegionComponent,
    RegionEditorComponent,
    RegionPickerComponent
  ],
  entryComponents : [
    RegionPickerComponent,
    DataSharingAgreementPickerComponent,
    DataProcessingAgreementPickerComponent,
    OrganisationPickerComponent,
  ],
  providers: [
    RegionService]
  // {provide: MapsAPILoader, useClass: CustomLazyAPIKeyLoader }]
})
export class RegionModule { }
