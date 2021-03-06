import {Component, OnInit, ViewChild} from '@angular/core';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {ActivatedRoute, Router} from '@angular/router';
import {GenericTableSspComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {OrganisationDialogComponent} from "../organisation-dialog/organisation-dialog.component";
import {UserProject} from "dds-angular8/user-manager";

@Component({
  selector: 'app-organisation',
  templateUrl: './organisation.component.html',
  styleUrls: ['./organisation.component.css']
})
export class OrganisationComponent implements OnInit {

  @ViewChild('organisationsTable', {static: false}) organisationsTable: GenericTableSspComponent;

  private paramSubscriber: any;
  organisations: Organisation[];
  modeType: string;
  searchData = '';
  searchType: string;
  subtitle: string;
  totalItems = 5;
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  superUser = false;
  userId: string;
  orgDetailsToShow = new Organisation().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.allowEdit = true;
      this.superUser = true;
      this.userId = null;
    } else if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    } else {
      this.allowEdit = false;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    }

    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode']);
      });
  }

  constructor(private organisationService: OrganisationService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService,
              public dialog: MatDialog) {
  }

  protected performAction(mode: string) {
    switch (mode.toLowerCase()) {
      case 'organisations':
        this.modeType = 'Organisation';
        this.searchType = 'organisation';
        this.subtitle = 'An organisation with an NHS ODS code';
        this.search();
        this.getTotalOrganisationCount();
        break;
      case 'services':
        this.modeType = 'Service';
        this.searchType = 'services';
        this.subtitle = 'A named service providing care, linked to an organisation';
        this.search();
        this.getTotalOrganisationCount();
        break;
    }
  }

  getTotalOrganisationCount() {

    this.organisationService.getTotalCount(this.searchData, this.searchType)
      .subscribe(
        (result) => {
          this.totalItems = result;
        },
        (error) => console.log(error)
      );
  }

  add() {
    if (this.modeType === 'Organisation') {
      const dialogRef = this.dialog.open(OrganisationDialogComponent, {
        data: {mode: 'add', uuid: '', orgType: this.modeType},
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.router.navigate(['/organisation', result.uuid, 'edit']);
          this.log.success('Organisation saved.');
        }
      });
    } else {
      const dialogRef = this.dialog.open(OrganisationDialogComponent, {
        data: {mode: 'addService', uuid: '', orgType: this.modeType},
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.router.navigate(['/organisation', result.uuid, 'edit']);
          this.log.success('Service saved.');
        }
      });
    }
  }

  edit(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete ' + this.modeType.toLowerCase() + 's', 'Are you sure you want to delete ' + this.modeType.toLowerCase() + 's?',
      'Delete ' + this.modeType.toLowerCase() + 's', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            let ids = [];
            for (var i = 0; i < this.organisationsTable.selection.selected.length; i++) {
              let org = this.organisationsTable.selection.selected[i];
              this.organisations.forEach( (item, index) => {
                if(item === org) {
                  ids.push(item.uuid);
                }
              });
            }
            this.organisationService.deleteOrganisation(ids).subscribe(
              () => {
                this.log.success('Delete successful.');
                this.search();
              }
            );
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  close() {
    this.router.navigate(['/overview']);
  }

  onSearch($event) {

    this.searchData = $event;
    this.pageNumber = 1;
    this.organisations = [];
    this.search();
    this.getTotalOrganisationCount();
  }

  private search() {

    this.loadingComplete = false;
    this.organisationService.search(this.searchData, this.searchType, this.pageNumber, this.pageSize, this.orderColumn, this.descending)
      .subscribe(result => {
          this.organisations = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The organisation could not be loaded. Please try again.');
          this.loadingComplete = true;
        }
      );
  }

  itemClicked(org: Organisation) {
    this.router.navigate(['/organisation', org.uuid, 'edit']);
  }

  pageChange($event) {
    this.pageNumber = $event.pageIndex + 1; // pagination index starts at 0, mySQL is 1
    this.pageSize = $event.pageSize;
    this.search();
  }

  onOrderChange($event) {
    this.orderColumn = $event.active;
    this.descending = $event.direction == 'desc' ? true : false;
    this.search();
  }
}
