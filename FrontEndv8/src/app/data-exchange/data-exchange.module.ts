import {NgModule} from '@angular/core';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from '@angular/platform-browser';
import {CommonModule} from '@angular/common';
import {CoreModule} from 'dds-angular8';
import {FlexModule} from '@angular/flex-layout';
import {FormsModule} from '@angular/forms';
import {
  MatBadgeModule, MatButtonModule,
  MatCardModule, MatCheckboxModule,
  MatDialogModule, MatDividerModule,
  MatFormFieldModule,
  MatIconModule, MatInputModule,
  MatMenuModule,
  MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule,
  MatSelectModule, MatSnackBarModule, MatSortModule,
  MatTableModule, MatTabsModule, MatTreeModule
} from '@angular/material';
import {RouterModule} from '@angular/router';

//import {DataExchangeComponent} from './data-exchange/data-exchange.component';
//import {DataExchangeEditorComponent} from './data-exchange-editor/data-exchange-editor.component';
//import {DataExchangePickerComponent} from './data-exchange-picker/data-exchange-picker.component';
import {DataExchangeService} from './data-exchange.service';

//TODO remove temp code -start
import {SchedulerComponent} from '../scheduler/scheduler/scheduler.component';
import {GenericTableModule} from '../generic-table/generic-table.module';
//TODO remove temp code -end

@NgModule({
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    CommonModule,
    CoreModule,
    FlexModule,
    FormsModule,
    GenericTableModule,
    MatBadgeModule, MatButtonModule,
    MatCardModule, MatCheckboxModule,
    MatDialogModule, MatDividerModule,
    MatFormFieldModule,
    MatIconModule, MatInputModule,
    MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule,
    MatSelectModule, MatSnackBarModule, MatSortModule,
    MatTableModule, MatTabsModule, MatTreeModule,
    RouterModule,
  ],
  declarations: [
    //DataExchangeComponent,
    //DataExchangeEditorComponent,
    //DataExchangePickerComponent
  ],
  entryComponents : [
    //DataExchangePickerComponent
    //TODO remove temp code -start
    SchedulerComponent,
    //TODO remove temp code -end
  ],
  providers: [
    DataExchangeService,
  ]
})
export class DataExchangeModule { }
