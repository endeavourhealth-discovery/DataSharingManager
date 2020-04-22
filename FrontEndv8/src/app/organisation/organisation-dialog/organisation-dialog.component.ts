import {Component, Inject, OnInit} from '@angular/core';
import {Organisation} from "../models/Organisation";
import {OrganisationType} from "../models/OrganisationType";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ItemLinkageService, LoggerService, UserManagerService} from "dds-angular8";
import {OrganisationService} from "../organisation.service";
import {DatePipe} from "@angular/common";
import {Region} from "../../region/models/Region";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Address} from "../models/Address";
import {UserProject} from "dds-angular8/user-manager";

export interface DialogData {
  mode: string;
  uuid: string;
  orgType: string;
}

@Component({
  selector: 'app-organisation-dialog',
  templateUrl: './organisation-dialog.component.html',
  styleUrls: ['./organisation-dialog.component.scss']
})
export class OrganisationDialogComponent implements OnInit {

  organisation: Organisation = <Organisation>{};
  orgType = 'Organisation';
  organisationTypes: OrganisationType[];

  systemSupplierSystems = this.linkageService.systemSupplierSystems;
  systemSupplierSharingActivated = this.linkageService.systemSupplierSharingActivated;
  public activeProject: UserProject;
  mode: string;
  uuid: string;
  regions: Region[] = [];
  childOrganisations: Organisation[] = [];
  parentOrganisations: Organisation[] = [];
  services: Organisation[] = [];
  addresses: Address[] = [];
  dpaPublishing: Dpa[] = [];
  dsaPublishing: Dsa[] = [];
  dsaSubscribing: Dsa[] = [];

  constructor(public dialogRef: MatDialogRef<OrganisationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private log: LoggerService,
              private orgService: OrganisationService,
              private userManagerNotificationService: UserManagerService,
              private datePipe: DatePipe,
              public dialog: MatDialog,
              private linkageService: ItemLinkageService) {

    this.uuid = data.uuid;
    this.mode = data.mode;
    this.orgType = data.orgType;
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    this.getOrganisationTypes();
    this.performAction(this.mode, this.uuid);
  }

  protected performAction(action: string, itemUuid: string) {
    switch (action) {
      case 'add':
        this.create();
        break;
      case 'addService':
        this.createService();
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create() {
    this.organisation = {
      name: '',
      isService: 0,
      bulkImported : 0,
      bulkItemUpdated : 0
    } as Organisation;
  }


  createService() {
    this.orgType = 'Service';
    this.organisation = {
      name: '',
      isService: 1,
      bulkImported : 0,
      bulkItemUpdated : 0
    } as Organisation;
  }

  load(uuid: string) {
    this.orgService.getOrganisation(uuid)
      .subscribe(result =>  {
          this.organisation = result;
          this.organisation.dateOfRegistration = this.datePipe.transform(this.organisation.dateOfRegistration,"yyyy-MM-dd");
        },
        error => this.log.error('The ' + this.orgType + ' could not be loaded. Please try again.')
      );
  }

  private getOrganisationTypes() {
    this.orgService.getOrganisationTypes()
      .subscribe(
        result => {this.organisationTypes = result;
        },
        error => this.log.error('The organisation types could not be loaded. Please try again.')
      );
  }

  ok() {
    this.organisation.regions = null;
    this.organisation.childOrganisations = null;
    this.organisation.parentOrganisations = null;
    this.organisation.services = null;
    this.organisation.dpaPublishing = null;
    this.organisation.dsaPublishing = null;
    this.organisation.dsaSubscribing = null;
    this.organisation.addresses = null;

    this.orgService.saveOrganisation(this.organisation)
      .subscribe(saved => {
          this.organisation.uuid = saved;
          this.dialogRef.close(this.organisation);
        },
        error => this.log.error('The ' + this.orgType + ' could not be saved. Please try again.')
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
