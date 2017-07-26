import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataSetComponent } from './data-set/data-set.component';
import { DataSetEditorComponent } from './data-set-editor/data-set-editor.component';
import { DataSetPickerComponent } from './data-set-picker/data-set-picker.component';
import {DataSetService} from './data-set.service';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {EntityViewComponentsModule} from 'eds-angular4';
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    EntityViewComponentsModule
  ],
  declarations: [
    DataSetComponent,
    DataSetEditorComponent,
    DataSetPickerComponent],
  providers: [DataSetService]
})
export class DataSetModule { }
