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
import {CoreModule} from "dds-angular8";
import {RouterModule} from "@angular/router";
import {FlexModule} from "@angular/flex-layout";
import {BrowserModule} from "@angular/platform-browser";
import {GenericTableModule} from "../generic-table/generic-table.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule} from "@angular/forms";

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
    RegionEditorComponent
  ],
  entryComponents : [
  ],
  providers: [
    RegionService]
  // {provide: MapsAPILoader, useClass: CustomLazyAPIKeyLoader }]
})
export class RegionModule { }
