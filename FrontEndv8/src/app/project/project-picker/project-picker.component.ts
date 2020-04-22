import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {GenericTableComponent, LoggerService, UserManagerService} from "dds-angular8";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Project} from "../models/Project";
import {ProjectService} from "../project.service";
import {StandardPickerData} from "../../models/StandardPickerData";
import {UserProject} from "dds-angular8/user-manager";

@Component({
  selector: 'app-project-picker',
  templateUrl: './project-picker.component.html',
  styleUrls: ['./project-picker.component.scss']
})
export class ProjectPickerComponent implements OnInit {

  searchData: string;
  searchResults: Project[];
  projectDetailsToShow = new Project().getDisplayItems();
  allowEdit = true;
  userId: string;
  public activeProject: UserProject;

  @ViewChild('picker', { static: false }) picker: GenericTableComponent;

  constructor(public dialogRef: MatDialogRef<ProjectPickerComponent>,
              @Inject(MAT_DIALOG_DATA) public data: StandardPickerData,
              private log: LoggerService,
              private userManagerNotificationService: UserManagerService,
              private service: ProjectService) {
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

  filterResults(results: Project[]) {
    let filterResults: Project[];
    const existing = this.data.existing;

    filterResults = results.filter((x) => !existing.filter(y => y.uuid === x.uuid).length);

    return filterResults;
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
    this.service.getAllProjects(this.userId)
      .subscribe(
        result => {
          this.searchResults = this.filterResults(result);
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
