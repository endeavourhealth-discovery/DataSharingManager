import {Component, OnInit, ViewChild} from '@angular/core';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {ActivatedRoute, Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableSspComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {DataSetDialogComponent} from "../../data-set/data-set-dialog/data-set-dialog.component";
import {OrganisationDialogComponent} from "../organisation-dialog/organisation-dialog.component";

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
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {


    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
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
        width: '1200px',
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
        width: '1200px',
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
    MessageBoxDialogComponent.open(this.dialog, 'Delete ' + this.modeType.toLowerCase(), 'Are you sure you want to delete ' + this.modeType.toLowerCase() + '(s)?',
      'Delete ' + this.modeType.toLowerCase(), 'Cancel')
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
          this.log.error('The organisation could not be loaded. Please try again.'/*, error, 'Load organisations'*/);
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
