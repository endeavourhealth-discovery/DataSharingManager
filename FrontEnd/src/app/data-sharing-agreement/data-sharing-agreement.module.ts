import { NgModule } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import { DataSharingAgreementComponent } from './data-sharing-agreement/data-sharing-agreement.component';
import { DataSharingAgreementEditorComponent } from './data-sharing-agreement-editor/data-sharing-agreement-editor.component';
import { DataSharingAgreementPickerComponent } from './data-sharing-agreement-picker/data-sharing-agreement-picker.component';
import { PurposeAddComponent } from './purpose-add/purpose-add.component';
import {DataSharingAgreementService} from './data-sharing-agreement.service';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {DialogsModule} from 'eds-angular4';
import {FormsModule} from '@angular/forms';
import { ToastModule } from 'ng2-toastr/ng2-toastr';
import {EntityViewComponentsModule} from "eds-angular4/dist/entityViewer";
import {GoogleMapsModule} from "eds-angular4/dist/googleMaps";
import {ControlsModule} from "eds-angular4/dist/controls";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    EntityViewComponentsModule,
    ToastModule.forRoot(),
    GoogleMapsModule,
    ControlsModule,
    DialogsModule
  ],
  declarations: [
    DataSharingAgreementComponent,
    DataSharingAgreementEditorComponent,
    DataSharingAgreementPickerComponent,
    PurposeAddComponent],
  entryComponents : [
    PurposeAddComponent,
    DataSharingAgreementPickerComponent
  ],
  providers: [DataSharingAgreementService, DatePipe]
})
export class DataSharingAgreementModule { }
