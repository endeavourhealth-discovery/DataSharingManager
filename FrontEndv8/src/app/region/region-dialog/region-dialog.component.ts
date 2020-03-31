import {Component, Inject, OnInit} from '@angular/core';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Region} from "../models/Region";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {LoggerService, UserManagerService} from "dds-angular8";
import {RegionService} from "../region.service";

export interface DialogData {
  mode: string;
  uuid: string;
}

@Component({
  selector: 'app-region-dialog',
  templateUrl: './region-dialog.component.html',
  styleUrls: ['./region-dialog.component.scss']
})
export class RegionDialogComponent implements OnInit {

  region: Region = <Region>{};
  superUser = false;
  userId: string;
  mode: string;
  uuid: string;
  public activeProject: UserProject;

  constructor(public dialogRef: MatDialogRef<RegionDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private regionService: RegionService,
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
    this.region = {
      name : ''
    } as Region;
  }

  load(uuid: string) {
    this.regionService.getRegion(uuid)
      .subscribe(result => {
          this.region = result;
        },
        error => this.log.error('The region could not be loaded. Please try again.')
      );
  }

  ok() {
    this.regionService.saveRegion(this.region)
      .subscribe(saved => {
          this.region.uuid = saved;
          this.dialogRef.close(this.region);
        },
        error => this.log.error('The region could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
