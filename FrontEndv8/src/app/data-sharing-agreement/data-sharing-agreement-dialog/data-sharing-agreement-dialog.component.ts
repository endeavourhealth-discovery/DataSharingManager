import {Component, Inject, OnInit} from '@angular/core';
import {Dsa} from "../models/Dsa";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ItemLinkageService, LoggerService, UserManagerService} from "dds-angular8";
import {DatePipe} from "@angular/common";
import {DialogData} from "../../data-processing-agreement/data-processing-agreement-dialog/data-processing-agreement-dialog.component";
import {DataSharingAgreementService} from "../data-sharing-agreement.service";
import {UserProject} from "dds-angular8/user-manager";

export interface DialogData {
  mode: string;
  uuid: string;
}

@Component({
  selector: 'app-data-sharing-agreement-dialog',
  templateUrl: './data-sharing-agreement-dialog.component.html',
  styleUrls: ['./data-sharing-agreement-dialog.component.scss']
})
export class DataSharingAgreementDialogComponent implements OnInit {

  dsa: Dsa = <Dsa>{};
  disableStatus = false;
  superUser = false;
  userId: string;
  mode: string;
  uuid: string;
  status = this.linkageService.status;
  consents = this.linkageService.consents;

  public activeProject: UserProject;

  constructor(public dialogRef: MatDialogRef<DataSharingAgreementDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private dsaService: DataSharingAgreementService,
              private datePipe: DatePipe,
              private userManagerNotificationService: UserManagerService,
              public dialog: MatDialog,
              private linkageService: ItemLinkageService) {

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
    this.dsa = {
      name : ''
    } as Dsa;
  }

  load(uuid: string) {
    this.dsaService.getDsa(uuid)
      .subscribe(result => {
          this.dsa = result;
          this.dsa.startDate = this.datePipe.transform(this.dsa.startDate,"yyyy-MM-dd");
          this.dsa.endDate = this.datePipe.transform(this.dsa.endDate,"yyyy-MM-dd");
          this.checkEndDate();
        },
        error => this.log.error('The data sharing agreement could not be loaded. Please try again.')
      );
  }

  checkEndDate() {
    if (this.dsa.endDate === null) {
      this.disableStatus = false;
      return;
    }

    let today = new Date();
    today.setHours(0,0,0,0);
    let endDate = new Date(this.dsa.endDate);

    if (endDate < today) {
      this.dsa.dsaStatusId = 1;
      this.disableStatus = true;
    } else {
      this.disableStatus = false;
    }
  }

  ok() {
    this.dsaService.saveDsa(this.dsa)
      .subscribe(saved => {
          this.dsa.uuid = saved;
          this.dialogRef.close(this.dsa);
        },
        error => this.log.error('The data sharing agreement could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
