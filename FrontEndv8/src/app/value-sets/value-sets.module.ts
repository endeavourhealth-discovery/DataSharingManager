import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ValueSetsComponent} from './value-sets/value-sets.component';
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {GenericTableModule} from "../generic-table/generic-table.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatMenuModule,
    GenericTableModule,
    MatDialogModule,
    MatButtonModule,
  ],
  declarations: [
    ValueSetsComponent,
  ],
})
export class ValueSetsModule { }
