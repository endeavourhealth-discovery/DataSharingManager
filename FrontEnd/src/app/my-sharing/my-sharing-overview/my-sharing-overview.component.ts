import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {User} from "eds-angular4/dist/security/models/User";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationService} from "../../organisation/organisation.service";
import {LoggerService, SecurityService} from "eds-angular4";
import {Router} from "@angular/router";
import {ToastsManager} from "ng2-toastr";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";

@Component({
  selector: 'app-my-sharing-overview',
  templateUrl: './my-sharing-overview.component.html',
  styleUrls: ['./my-sharing-overview.component.css']
})
export class MySharingOverviewComponent implements OnInit {
  currentUser: User;
  userOrgs : string[] = [];
  currentOrg = 'c45ccafd-f86a-4778-845a-96269cad6c3d';  // d79f403b-963d-4817-b4a5-0fbf6a516cb0
  dpaPublishing: Dpa[];
  dsaPublishing: Dsa[];
  dsaSubscribing: Dsa[];
  dsaPubLoadingComplete = false;
  dsaSubLoadingComplete = false;
  dpaPubLoadingComplete = false;

  dpaDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private organisationService: OrganisationService,
              private log: LoggerService,
              private securityService: SecurityService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    const vm = this;
    vm.currentUser = vm.securityService.getCurrentUser();
    console.log(vm.currentUser);
    vm.getOrganisationsForUser(vm.currentUser);
    // vm.currentOrg = vm.currentUser.organisation;
  }

  private getOrganisationsForUser(user: User) {
    const vm = this;
    vm.userOrgs = user.organisationGroups.map(a => a.organisationId);
    console.log(vm.userOrgs);
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
