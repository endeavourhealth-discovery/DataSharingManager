import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {GenericTableComponent, LoggerService, UserManagerService} from "dds-angular8";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Dsa} from "../models/Dsa";
import {DataSharingAgreementService} from "../data-sharing-agreement.service";
import {UserProject} from "dds-angular8/user-manager";

export interface DialogData {
  allowMultiple: boolean;
  existing: any[];
}

@Component({
  selector: 'app-data-sharing-agreement-picker',
  templateUrl: './data-sharing-agreement-picker.component.html',
  styleUrls: ['./data-sharing-agreement-picker.component.scss']
})
export class DataSharingAgreementPickerComponent implements OnInit {

  searchData: string;
  searchResults: Dsa[];
  dsaDetailsToShow = new Dsa().getDisplayItems();
  allowEdit = true;
  userId: string;
  allowMultiple: boolean;
  public activeProject: UserProject;

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(public dialogRef: MatDialogRef<DataSharingAgreementPickerComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private userManagerNotificationService: UserManagerService,
              private service: DataSharingAgreementService) {
    this.allowMultiple = data.allowMultiple;
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.allowEdit = true;
      this.userId = null;
    } else if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
      this.userId = this.activeProject.userId;
    } else {
      this.allowEdit = false;
      this.userId = this.activeProject.userId;
    }
    this.searchAll();
  }

  clear() {
    this.searchData = '';
    this.searchAll();
  }

  search($event: KeyboardEvent) {
    if (this.searchData.length < 3) {
      return;
    }
    this.service.search(this.searchData)
      .subscribe(
        result => this.searchResults = this.filterResults(result),
        (error) => this.log.error(error)
      );
  }

  searchAll() {
    this.service.getAllDsas(this.userId)
      .subscribe(
        result => {
          this.searchResults = this.filterResults(result);
        }
      );
  }

  filterResults(results: Dsa[]) {
    let filterResults: Dsa[];
    const existing = this.data.existing;

    filterResults = results.filter((x) => !existing.filter(y => y.uuid === x.uuid).length);

    return filterResults;
  }

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }
}
