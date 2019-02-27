import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DialogsModule, LoggerService} from "eds-angular4";
import {ControlsModule} from "eds-angular4/dist/controls";
import {ToastModule} from "ng2-toastr";
import {FormsModule} from "@angular/forms";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ConfigService} from "./config.service";
import { ConfigComponent } from './config/config.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ToastModule.forRoot(),
    NgbModule,
    DialogsModule,
    ControlsModule
  ],
  declarations: [ConfigComponent],
  providers: [
    ConfigService,
    LoggerService
  ]
})
export class ConfigModule { }
