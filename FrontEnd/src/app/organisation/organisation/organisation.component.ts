import { Component, OnInit } from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Organisation} from '../models/Organisation';
import {LoggerService, MessageBoxDialog, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {OrganisationService} from '../organisation.service';
import {ActivatedRoute, Router} from '@angular/router';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-organisation',
  templateUrl: './organisation.component.html',
  styleUrls: ['./organisation.component.css']
})
export class OrganisationComponent implements OnInit {
  private paramSubscriber: any;
  organisations: Organisation[];
  modeType: string;
  searchData = '';
  searchType: string;
  totalItems = 5;
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  orgDetailsToShow = new Organisation().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  ngOnInit() {
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode']);
      });

    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
  }

  constructor(private $modal: NgbModal,
              private organisationService: OrganisationService,
              private securityService: SecurityService,
              private log: LoggerService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerNotificationService) {
  }

  protected performAction(mode: string) {
    switch (mode) {
      case 'organisations':
        this.modeType = 'Organisation';
        this.searchType = 'organisation';
        this.search();
        this.getTotalOrganisationCount();
        break;
      case 'services':
        this.modeType = 'Service';
        this.searchType = 'services';
        this.search();
        this.getTotalOrganisationCount();
        break;
    }
  }

  getTotalOrganisationCount() {
    const vm = this;
    vm.organisationService.getTotalCount(vm.searchData, vm.searchType)
      .subscribe(
        (result) => {
          vm.totalItems = result;
        },
        (error) => console.log(error)
      );
  }

  add() {
    if (this.modeType === 'Organisation') {
      this.router.navigate(['/organisation', 1, 'add']);
    } else {
      this.router.navigate(['/organisation', 1, 'addService']);
    }
  }

  edit(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  save(original: Organisation, edited: Organisation) {
    const vm = this;
    vm.organisationService.saveOrganisation(edited)
      .subscribe(
        saved =>  {
          if (original.uuid) {
            console.log('fix this');
            // jQuery.extend(true, original, saved);
          } else {
            vm.organisations.push(saved);
          }

          vm.log.success('Organisation saved', original, 'Save organisation');
        },
        error => vm.log.error('The organisation could not be saved. Please try again.', error, 'Save organisation')
      );
  }

  delete(item: Organisation) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete organisation', 'Are you sure that you want to delete <b>' + item.name + '</b>?', 'Delete organisation', 'Cancel')
      .result.then(
      () => vm.doDelete(item),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(item: Organisation) {
    const vm = this;
    vm.organisationService.deleteOrganisation(item.uuid)
      .subscribe(
        () => {
          vm.search();
          vm.log.success('Organisation deleted', item, 'Delete organisation');
        },
        (error) => vm.log.error('The organisation could not be deleted. Please try again.', error, 'Delete organisation')
      );
  }

  close() {
    this.router.navigate(['/organisationOverview']);
  }

  onSearch($event) {
    const vm = this;
    vm.searchData = $event;
    vm.pageNumber = 1;
    vm.organisations = [];
    vm.search();
    vm.getTotalOrganisationCount();
  }

  private search() {
    const vm = this;
    vm.loadingComplete = false;
    vm.organisationService.search(vm.searchData, vm.searchType, vm.pageNumber, vm.pageSize, vm.orderColumn, vm.descending)
      .subscribe(result => {
          vm.organisations = result;
          vm.loadingComplete = true;
        },
        error => {
          vm.log.error('The organisation could not be loaded. Please try again.', error, 'Load organisations');
          vm.loadingComplete = true;
        }
      );
  }

  pageChange($event) {
    const vm = this;
    vm.pageNumber = $event;
    vm.search();
  }

  pageSizeChange($event) {
    const vm = this;
    vm.pageSize = $event;
    vm.search();
  }

  onOrderChange($event) {
    const vm = this;
    vm.orderColumn = $event.column;
    vm.descending = $event.descending;
    vm.search();
  }
}
