import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProjectComponent} from './project/project.component';
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {CoreModule, GenericTableModule, ItemLinkageModule, MessageBoxDialogComponent} from "dds-angular8";
import {FlexModule} from "@angular/flex-layout";
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {MatButtonModule} from "@angular/material/button";
import {MatBadgeModule} from "@angular/material/badge";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDialogModule} from "@angular/material/dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSelectModule} from "@angular/material/select";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatTreeModule} from "@angular/material/tree";
import {MatTabsModule} from "@angular/material/tabs";
import {MatTableModule} from "@angular/material/table";
import {MatSortModule} from "@angular/material/sort";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatListModule} from "@angular/material/list";
import {ProjectEditorComponent} from './project-editor/project-editor.component';
import {DataSetPickerComponent} from "../data-set/data-set-picker/data-set-picker.component";
import {CohortPickerComponent} from "../cohort/cohort-picker/cohort-picker.component";
import {DataSharingAgreementPickerComponent} from "../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {ProjectPickerComponent} from './project-picker/project-picker.component';
import {ValueSetsComponent} from "../value-sets/value-sets/value-sets.component";
import {ProjectDialogComponent} from './project-dialog/project-dialog.component';
import { ExtractDetailsDialogComponent } from './extract-details-dialog/extract-details-dialog.component';

@NgModule({
  declarations: [ProjectComponent, ProjectEditorComponent, ProjectPickerComponent, ProjectDialogComponent, ExtractDetailsDialogComponent],
    imports: [
        BrowserAnimationsModule,
        BrowserModule,
        CommonModule,
        CoreModule,
        FlexModule,
        FormsModule,
        GenericTableModule, ItemLinkageModule,
        MatBadgeModule, MatButtonModule,
        MatCardModule, MatCheckboxModule,
        MatDialogModule, MatDividerModule,
        MatFormFieldModule,
        MatIconModule, MatInputModule,
        MatMenuModule,
        MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule,
        MatSelectModule, MatSnackBarModule, MatSortModule,
        MatTableModule, MatTabsModule, MatTreeModule,
        RouterModule, MatListModule,
    ],
  entryComponents : [
    DataSetPickerComponent,
    CohortPickerComponent,
    MessageBoxDialogComponent,
    DataSharingAgreementPickerComponent,
    ValueSetsComponent,
    ProjectDialogComponent,
    ExtractDetailsDialogComponent,
  ],
})
export class ProjectModule { }
