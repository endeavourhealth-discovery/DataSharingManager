import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {OrganisationService} from "../../organisation/organisation.service";
import {Router} from "@angular/router";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";
import {Project} from "../../project/models/Project";

@Component({
  selector: 'app-my-sharing-overview',
  templateUrl: './my-sharing-overview.component.html',
  styleUrls: ['./my-sharing-overview.component.css']
})
export class MySharingOverviewComponent implements OnInit {
  userOrgs : string[] = [];
  dpaPublishing: Dpa[];
  projectPublishing: Project[];
  dsaPublishing: Dsa[];
  dsaSubscribing: Dsa[];
  dsaPubLoadingComplete = false;
  projectsPubLoadingComplete = false;
  dsaSubLoadingComplete = false;
  dpaPubLoadingComplete = false;
  allowEdit = false;

  public activeProject: UserProject;

  dpaDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();
  projectDetailsToShow = new Project().getDisplayItems();

  constructor(private organisationService: OrganisationService,
              private log: LoggerService,
              private router: Router,
              private userManagerService: UserManagerService) {  }

  ngOnInit() {


    this.userManagerService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {

    this.userOrgs = [];
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }
    this.userOrgs.push(this.activeProject.organisationId);
    this.getDPAsPublishingTo(this.userOrgs);
    this.getDSAsPublishingTo(this.userOrgs);
    this.getDSAsSubscribingTo(this.userOrgs);
    this.getProjectsPublishingTo(this.userOrgs);

  }

  private getDPAsPublishingTo(orgs: string[]) {

    this.dpaPubLoadingComplete = false;
    this.organisationService.getDPAPublishingFromList(orgs)
      .subscribe(
        result => {
          this.dpaPublishing = result;
          this.dpaPubLoadingComplete = true;
          },
        error => {
          this.log.error('The associated publishing data processing agreements could not be loaded. Please try again.');
          this.dpaPubLoadingComplete = true;
        }
      );
  }

  private getProjectsPublishingTo(orgs: string[]) {

    this.projectsPubLoadingComplete = false;
    this.organisationService.getProjectsPublishingFromList(orgs)
      .subscribe(
        result => {
          this.projectPublishing = result;
          this.projectsPubLoadingComplete = true;
        },
        error => {
          this.log.error('The associated publishing projects could not be loaded. Please try again.');
          this.dpaPubLoadingComplete = true;
        }
      );
  }

  private getDSAsPublishingTo(orgs: string[]) {

    this.dsaPubLoadingComplete = false;
    this.organisationService.getDSAPublishingFromList(orgs)
      .subscribe(
        result => {
          this.dsaPublishing = result;
          this.dsaPubLoadingComplete = true;
          },
        error => {
          this.log.error('The associated publishing data sharing agreements could not be loaded. Please try again.');
          this.dsaPubLoadingComplete = true;
        }
      );
  }

  private getDSAsSubscribingTo(orgs: string[]) {

    this.dsaSubLoadingComplete = false;
    this.organisationService.getDSASubscribingFromList(orgs)
      .subscribe(
        result => {
          this.dsaSubscribing = result;
          this.dsaSubLoadingComplete = true;
          },
        error => {
          this.log.error('The associated subscribing data sharing agreements could not be loaded. Please try again.');
          this.dsaSubLoadingComplete = true;
        }
      );
  }

  dsaClicked(dsa: Dsa) {
    this.router.navigate(['/dsa', dsa.uuid, 'edit']);
  }

  dpaClicked(dpa: Dpa) {
    this.router.navigate(['/dpa', dpa.uuid, 'edit']);
  }

  projectClicked(project: Project) {
    this.router.navigate(['/project', project.uuid, 'edit']);
  }
}
