import { NgModule } from '@angular/core';
import {ConceptLibraryComponent} from './concept-library/concept-library.component';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {
  MatCardModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatPaginatorModule, MatProgressSpinnerModule,
  MatSortModule,
  MatTableModule
} from '@angular/material';
import {FormsModule} from '@angular/forms';
import { ConceptEditorComponent } from './concept-editor/concept-editor.component';
import {RouterModule} from '@angular/router';
import {FlexModule} from '@angular/flex-layout';
import {MatSelectModule} from '@angular/material/select';
import { ConceptDefinitionComponent } from './concept-definition/concept-definition.component';
import { ConceptExpressionComponent } from './concept-expression/concept-expression.component';
import { AttributeExpressionComponent } from './attribute-expression/attribute-expression.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {RoleGroupComponent} from './role-group/role-group.component';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatMenuModule} from '@angular/material/menu';
import {CoreModule} from 'dds-angular8';
import { ParentHierarchyDialogComponent } from './parent-hierarchy-dialog/parent-hierarchy-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatTreeModule} from '@angular/material/tree';
import {ChildHierarchyDialogComponent} from './child-hierarchy-dialog/child-hierarchy-dialog.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';



@NgModule({
  declarations: [
    ConceptLibraryComponent,
    ConceptEditorComponent,
    ConceptDefinitionComponent,
    ConceptExpressionComponent,
    AttributeExpressionComponent,
  RoleGroupComponent,
  ParentHierarchyDialogComponent,
  ChildHierarchyDialogComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
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
  entryComponents: [
    ParentHierarchyDialogComponent,
    ChildHierarchyDialogComponent
  ]
})
export class ConceptModule { }
