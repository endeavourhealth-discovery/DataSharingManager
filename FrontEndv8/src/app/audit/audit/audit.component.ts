import { Component, OnInit } from '@angular/core';
import {UserProject} from "dds-angular8/user-manager/models/UserProject";
import {LoggerService} from "dds-angular8/logger";
import {UserManagerService} from "dds-angular8/user-manager";

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent implements OnInit {

  public activeProject: UserProject;
  admin = false;
  superUser = false;
  userOrganisationId : string;

  constructor(public log:LoggerService,
              private userManagerService: UserManagerService) { }

  ngOnInit() {

    this.userManagerService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

  }

  roleChanged() {

    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.admin = true;
      this.superUser = true;
    } else if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.admin = true;
      this.superUser = false;
    } else {
      this.admin = false;
      this.superUser = false;
    }

  }

}

