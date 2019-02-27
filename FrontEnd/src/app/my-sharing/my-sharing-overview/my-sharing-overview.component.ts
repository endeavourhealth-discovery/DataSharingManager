import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {User} from "eds-angular4/dist/security/models/User";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationService} from "../../organisation/organisation.service";
import {LoggerService, SecurityService, UserManagerNotificationService} from "eds-angular4";
import {Router} from "@angular/router";
import {ToastsManager} from "ng2-toastr";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-my-sharing-overview',
  templateUrl: './my-sharing-overview.component.html',
  styleUrls: ['./my-sharing-overview.component.css']
})
export class MySharingOverviewComponent implements OnInit {
  currentUser: User;
  userOrgs : string[] = [];
  dpaPublishing: Dpa[];
  dsaPublishing: Dsa[];
  dsaSubscribing: Dsa[];
  dsaPubLoadingComplete = false;
  dsaSubLoadingComplete = false;
  dpaPubLoadingComplete = false;
  allowEdit = false;

  public activeProject: UserProject;

  dpaDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private organisationService: OrganisationService,
              private log: LoggerService,
              private securityService: SecurityService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    const vm = this;
    vm.currentUser = vm.securityService.getCurrentUser();

    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    const vm = this;
    vm.userOrgs = [];
    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
    vm.userOrgs.push(vm.activeProject.organisationId);
    vm.getDPAsPublishingTo(vm.userOrgs);
    vm.getDSAsPublishingTo(vm.userOrgs);
    vm.getDSAsSubscribingTo(vm.userOrgs);

  }

  private getDPAsPublishingTo(orgs: string[]) {
    const vm = this;
    vm.dpaPubLoadingComplete = false;
    vm.organisationService.getDPAPublishingFromList(orgs)
      .subscribe(
        result => {
          vm.dpaPublishing = result;
          vm.dpaPubLoadingComplete = true;
          },
        error => {
          vm.log.error('The associated publishing data processing agreements could not be loaded. Please try again.', error, 'Load associated publishing data processing agreements');
          vm.dpaPubLoadingComplete = true;
        }
      );
  }

  private getDSAsPublishingTo(orgs: string[]) {
    const vm = this;
    vm.dsaPubLoadingComplete = false;
    vm.organisationService.getDSAPublishingFromList(orgs)
      .subscribe(
        result => {
          vm.dsaPublishing = result;
          vm.dsaPubLoadingComplete = true;
          },
        error => {
          vm.log.error('The associated publishing data sharing agreements could not be loaded. Please try again.', error, 'Load associated publishing data sharing agreements');
          vm.dsaPubLoadingComplete = true;
        }
      );
  }

  private getDSAsSubscribingTo(orgs: string[]) {
    const vm = this;
    vm.dsaSubLoadingComplete = false;
    vm.organisationService.getDSASubscribingFromList(orgs)
      .subscribe(
        result => {
          vm.dsaSubscribing = result;
          vm.dsaSubLoadingComplete = true;
          },
        error => {
          vm.log.error('The associated subscribing data sharing agreements could not be loaded. Please try again.', error, 'Load associated subscribing data sharing agreements');
          vm.dsaSubLoadingComplete = true;
        }
      );
  }
}
