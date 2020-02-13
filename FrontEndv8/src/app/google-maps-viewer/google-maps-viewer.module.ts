import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MatBadgeModule, MatButtonModule, MatCardModule, MatCheckboxModule,
  MatDialogModule, MatDividerModule, MatFormFieldModule, MatIconModule, MatInputModule,
  MatMenuModule, MatPaginatorModule, MatProgressBarModule,
  MatProgressSpinnerModule,
  MatSelectModule, MatSnackBarModule, MatSortModule, MatTableModule,
  MatTabsModule,
  MatTreeModule
} from "@angular/material";
import {BrowserModule} from "@angular/platform-browser";
import {FlexModule} from "@angular/flex-layout";
import {AgmCoreModule} from "@agm/core";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule} from "@angular/forms";
import {CoreModule, GenericTableModule} from "dds-angular8";
import {RouterModule} from "@angular/router";
import {GoogleMapsDialogComponent} from "./google-maps-dialog/google-maps-dialog.component";
import {AgmJsMarkerClustererModule} from "@agm/js-marker-clusterer";



@NgModule({
  declarations: [
    GoogleMapsDialogComponent
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
    GenericTableModule,

    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyD0vq83Q9bjIQH25R64p5RuCquDo56gP0Y'
    }),
    AgmJsMarkerClustererModule
  ],
  entryComponents : [
    GoogleMapsDialogComponent
  ]
})
export class GoogleMapsViewerModule { }
