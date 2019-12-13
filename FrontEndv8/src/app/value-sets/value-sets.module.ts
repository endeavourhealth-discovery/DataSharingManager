import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ValueSetsComponent } from './value-sets/value-sets.component';
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";



@NgModule({
  declarations: [ValueSetsComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatMenuModule
  ]
})
export class ValueSetsModule { }
