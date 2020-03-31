import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PurposeComponent} from './purpose/purpose.component';
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {FlexModule} from "@angular/flex-layout";
import {CoreModule} from "dds-angular8";
import {MatBadgeModule} from "@angular/material/badge";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [PurposeComponent],
  imports: [
    CommonModule,
    CoreModule,
    FlexModule,
    FormsModule,
    MatBadgeModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
  ],
  entryComponents : [
    PurposeComponent
  ],
})
export class PurposeModule { }
