import {Component, OnInit, ViewChild} from '@angular/core';
import {Dpa} from "../models/Dpa";
import {MatDialogRef} from "@angular/material/dialog";
import {GenericTableComponent, LoggerService, UserManagerService} from "dds-angular8";
import {DataProcessingAgreementService} from "../data-processing-agreement.service";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";

@Component({
  selector: 'app-data-processing-agreement-picker',
  templateUrl: './data-processing-agreement-picker.component.html',
  styleUrls: ['./data-processing-agreement-picker.component.scss']
})
export class DataProcessingAgreementPickerComponent implements OnInit {

  searchData: string;
  searchResults: Dpa[];
  dpaDetailsToShow = new Dpa().getDisplayItems();
  allowEdit = true;
  userId: string;
  public activeProject: UserProject;

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(public dialogRef: MatDialogRef<DataProcessingAgreementPickerComponent>,
              private log: LoggerService,
              private userManagerNotificationService: UserManagerService,
              private service: DataProcessingAgreementService) {
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
        result => this.searchResults = result,
        (error) => this.log.error(error)
      );
  }

  searchAll() {
    this.service.getAllDpas(this.userId)
      .subscribe(
        result => {
          this.searchResults = result;
        }
      );
  }

  ok() {
    this.dialogRef.close(this.picker.selection.selected);
  }

  cancel() {
    this.dialogRef.close();
  }
}
