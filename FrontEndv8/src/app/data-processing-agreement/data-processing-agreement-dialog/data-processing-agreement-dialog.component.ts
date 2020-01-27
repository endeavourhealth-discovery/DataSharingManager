import {Component, Inject, OnInit} from '@angular/core';
import {Dpa} from "../models/Dpa";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {LoggerService, UserManagerService} from "dds-angular8";
import {DataProcessingAgreementService} from "../data-processing-agreement.service";
import {DatePipe} from "@angular/common";

export interface DialogData {
  mode: string;
  uuid: string;
}

@Component({
  selector: 'app-data-processing-agreement-dialog',
  templateUrl: './data-processing-agreement-dialog.component.html',
  styleUrls: ['./data-processing-agreement-dialog.component.scss']
})
export class DataProcessingAgreementDialogComponent implements OnInit {

  dpa: Dpa;
  public activeProject: UserProject;
  mode: string;
  uuid: string;
  status = [
    {num: 0, name: 'Active'},
    {num: 1, name: 'Inactive'}
  ];
  disableStatus = false;
  processor = 'Discovery';

  constructor(public dialogRef: MatDialogRef<DataProcessingAgreementDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private dpaService: DataProcessingAgreementService,
              private datePipe: DatePipe,
              private userManagerNotificationService: UserManagerService,
              public dialog: MatDialog) {

    this.uuid = data.uuid;
    this.mode = data.mode;
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    this.performAction(this.mode, this.uuid);
  }

  protected performAction(action: string, itemUuid: string) {
    switch (action) {
      case 'add':
        this.create();
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create() {
    this.dpa = {
      name : ''
    } as Dpa;
  }

  load(uuid: string) {
    this.dpaService.getDpa(uuid)
      .subscribe(result => {
          this.dpa = result;
          this.dpa.startDate = this.datePipe.transform(this.dpa.startDate,"yyyy-MM-dd");
          this.dpa.endDate = this.datePipe.transform(this.dpa.endDate,"yyyy-MM-dd");
          this.checkEndDate();
        },
        error => this.log.error('The data processing agreement could not be loaded. Please try again.')
      );
  }

  checkEndDate() {
    if (this.dpa.endDate === null) {
      this.disableStatus = false;
      return;
    }

    let today = new Date();
    today.setHours(0,0,0,0);
    let endDate = new Date(this.dpa.endDate);

    if (endDate < today) {
      this.dpa.dsaStatusId = 1;
      this.disableStatus = true;
    } else {
      this.disableStatus = false;
    }
  }

  ok() {
    console.log(this.dpa);
    this.dpaService.saveDpa(this.dpa)
      .subscribe(saved => {
          this.dpa.uuid = saved;
          this.dialogRef.close(this.dpa);
        },
        error => this.log.error('The DPA could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
