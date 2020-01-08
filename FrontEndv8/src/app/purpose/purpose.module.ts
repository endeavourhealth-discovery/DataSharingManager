import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PurposeComponent} from './purpose/purpose.component';
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";



@NgModule({
  declarations: [PurposeComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatInputModule,
    FormsModule
  ],
  entryComponents : [
    PurposeComponent
  ],
})
export class PurposeModule { }
