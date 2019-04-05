import { NgModule } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import { OrganisationComponent } from './organisation/organisation.component';
import { OrganisationOverviewComponent } from './organisation-overview/organisation-overview.component';
import { OrganisationService } from './organisation.service';
import { LoggerService, DialogsModule } from 'eds-angular4';
import { ToastModule } from 'ng2-toastr/ng2-toastr';
import { OrganisationEditorComponent } from './organisation-editor/organisation-editor.component';
import { OrganisationPickerComponent } from './organisation-picker/organisation-picker.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {EntityViewComponentsModule} from "eds-angular4/dist/entityViewer";
import {ControlsModule} from "eds-angular4/dist/controls";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    EntityViewComponentsModule,
    ToastModule.forRoot(),
    NgbModule,
    DialogsModule,
    ControlsModule
  ],
  declarations: [
    OrganisationComponent,
    OrganisationOverviewComponent,
    OrganisationEditorComponent,
    OrganisationPickerComponent
  ],
  entryComponents : [
    OrganisationPickerComponent
  ],
  providers: [
    OrganisationService,
    LoggerService,
    DatePipe]
})
export class OrganisationModule { }
