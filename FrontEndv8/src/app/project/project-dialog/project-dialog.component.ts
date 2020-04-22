import {Component, Inject, OnInit} from '@angular/core';
import {ItemLinkageService, LoggerService, UserManagerService} from "dds-angular8";
import {ProjectService} from "../project.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Project} from "../models/Project";
import {DatePipe} from "@angular/common";
import {User} from "../models/User";
import {ApplicationPolicy} from "../models/ApplicationPolicy";
import {ProjectApplicationPolicy} from "../models/ProjectApplicationPolicy";
import {UserProject} from "dds-angular8/user-manager";

export interface DialogData {
  mode: string;
  uuid: string;
  project: Project;
}

@Component({
  selector: 'app-project-dialog',
  templateUrl: './project-dialog.component.html',
  styleUrls: ['./project-dialog.component.scss']
})
export class ProjectDialogComponent implements OnInit {

  project: Project;
  public activeProject: UserProject;
  mode: string;
  uuid: string;
  disableStatus = false;
  userList: User[] = [];
  availablePolicies: ApplicationPolicy[];
  selectedApplicationPolicy: ApplicationPolicy;
  projectApplicationPolicy: ProjectApplicationPolicy;

  businessCaseStatuses = this.linkageService.businessCaseStatuses;

  storageProtocols = this.linkageService.storageProtocols;

  consents = this.linkageService.consents;

  deidentificationLevel = this.linkageService.deidentificationLevel;

  projectTypes = this.linkageService.projectTypes;

  flowScheduleIds = this.linkageService.flowScheduleIds;

  outputFormat = this.linkageService.outputFormat;

  securityInfrastructures = this.linkageService.securityInfrastructures;

  securityArchitectures = this.linkageService.securityArchitectures;

  status = this.linkageService.status;


  constructor(public dialogRef: MatDialogRef<ProjectDialogComponent>,
              private log: LoggerService,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private projectService: ProjectService,
              private datePipe: DatePipe,
              private userManagerService: UserManagerService,
              public dialog: MatDialog,
              private linkageService: ItemLinkageService) {
    this.uuid = data.uuid;
    this.mode = data.mode;
    this.project = data.project;
  }

  ngOnInit() {
    this.userManagerService.onProjectChange.subscribe(active => {
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
    this.project = {
      name : ''
    } as Project;
  }

  load(uuid: string) {
    this.projectService.getProject(uuid)
      .subscribe(result =>  {
          this.project = result;
          this.project.startDate = this.datePipe.transform(this.project.startDate,"yyyy-MM-dd");
          this.project.endDate = this.datePipe.transform(this.project.endDate,"yyyy-MM-dd");
          this.checkEndDate();
          this.getProjectApplicationPolicy();
          this.getUserList();
        },
        error => this.log.error('The data processing agreement could not be loaded. Please try again.')
      );
  }

  checkEndDate() {
    if (this.project.endDate === null) {
      this.disableStatus = false;
      return;
    }

    let today = new Date();
    today.setHours(0,0,0,0);
    let endDate = new Date(this.project.endDate);

    if (endDate < today) {
      this.project.projectStatusId = 1;
      this.disableStatus = true;
    } else {
      this.disableStatus = false;
    }
  }

  getUserList() {
    this.projectService.getUsers()
      .subscribe(
        (result) => this.userList = result,
        (error) => this.log.error('User list could not be loaded. Please try again.')
      );
  }

  getProjectApplicationPolicy() {
    this.projectService.getAvailableProjectApplicationPolicy()
      .subscribe(
        (result) => {
          this.availablePolicies = result;
          this.projectService.getProjectApplicationPolicy(this.project.uuid)
            .subscribe(
              (result) => {
                this.projectApplicationPolicy = result;
                this.selectedApplicationPolicy = this.availablePolicies.find(r => {
                  return r.id === this.projectApplicationPolicy.applicationPolicyId;
                });
              },
              (error) => {
                this.log.error('Project application policy could not be loaded. Please try again.');
              }
            );
        },
        (error) => this.log.error('Available application policies could not be loaded. Please try again.')
      );
  }

  changeUserApplicationPolicy(policyId: string) {
    let changedPolicy = new ProjectApplicationPolicy();
    changedPolicy.projectUuid = this.project.uuid;
    changedPolicy.applicationPolicyId = policyId;
    this.projectApplicationPolicy = changedPolicy;
  }

  saveApplicationPolicy() {
    this.projectService.saveProjectApplicationPolicy(this.projectApplicationPolicy)
      .subscribe(
        (response) => {
        },
        (error) => this.log.error('Project application policy could not be saved. Please try again.')
      );
  }

  ok() {
    this.projectService.saveProject(this.project)
      .subscribe(saved => {
          this.project.uuid = saved;
          if (this.mode == 'edit') {
            this.saveApplicationPolicy();
          }
          this.dialogRef.close(this.project);
        },
        error => this.log.error('The project could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
