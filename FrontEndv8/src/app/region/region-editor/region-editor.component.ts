import {Component, OnInit, QueryList, ViewChild} from '@angular/core';
import {RegionService} from '../region.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Region} from "../models/Region";
import {Organisation} from "../../organisation/models/Organisation";
import {Dsa} from "../../data-sharing-agreement/model/Dsa";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Marker} from "../models/Marker";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {LoggerService, UserManagerService} from "dds-angular8";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";

@Component({
  selector: 'app-region-editor',
  templateUrl: './region-editor.component.html',
  styleUrls: ['./region-editor.component.css']
})
export class RegionEditorComponent implements OnInit {
  private paramSubscriber: any;

  region: Region = <Region>{};
  organisations: Organisation[] = [];
  parentRegions: Region[] = [];
  childRegions: Region[] = [];
  sharingAgreements: Dsa[] = [];
  processingAgreements: Dpa[] = [];
  markers: Marker[] = [];
  editDisabled = false;
  latitude: number = 33.8121;
  longitude: number = -117.918;
  zoom: number = 12;
  allowEdit = false;
  superUser = false;
  userId: string;

  @ViewChild('orgTable', { static: false }) orgTable: GenericTableComponent;
  @ViewChild('dsaTable', { static: false }) dsaTable: GenericTableComponent;
  @ViewChild('dpaTable', { static: false }) dpaTable: GenericTableComponent;
  @ViewChild('childRegionTable', { static: false }) childRegionTable: GenericTableComponent;
  @ViewChild('parentRegionTable', { static: false }) parentRegionTable: GenericTableComponent;

  public activeProject: UserProject;

  orgDetailsToShow = new Organisation().getDisplayItems();
  regionDetailsToShow = new Region().getDisplayItems();
  sharingAgreementsDetailsToShow = new Dsa().getDisplayItems();
  processingAgreementsDetailsToShow = new Dpa().getDisplayItems();

  constructor(private log: LoggerService,
              private regionService: RegionService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerService: UserManagerService) {}

  ngOnInit() {

    this.userManagerService.onProjectChange.subscribe(active => {
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
        this.performAction(params['mode'], params['id']);
      });
  }

  protected performAction(action: string, itemUuid: string) {
    switch (action) {
      case 'add':
        this.create(itemUuid);
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create(uuid: string) {
    this.region = {
      name : ''
    } as Region;
  }

  load(uuid: string) {

    this.regionService.getRegion(uuid)
      .subscribe(result =>  {
          this.region = result;
          this.getRegionOrganisations();
          this.getParentRegions();
          this.getChildRegions();
          this.getOrganisationMarkers();
          this.getSharingAgreements();
          this.getProcessingAgreements();
        },
        error => this.log.error('The region could not be loaded. Please try again.')
      );
  }

  save(close: boolean) {


    // Populate organisations before save
    this.region.organisations = {};
    for (const idx in this.organisations) {
      const organisation: Organisation = this.organisations[idx];
      this.region.organisations[organisation.uuid] = organisation.name;
    }

    // populate Parent Regions
    this.region.parentRegions = {};
    for (const idx in this.parentRegions) {
      const region: Region = this.parentRegions[idx];
      this.region.parentRegions[region.uuid] = region.name;
    }

    // populate Parent Regions
    this.region.childRegions = {};
    for (const idx in this.childRegions) {
      const region: Region = this.childRegions[idx];
      this.region.childRegions[region.uuid] = region.name;
    }

    // populate sharing agreements
    this.region.sharingAgreements = {};
    for (const idx in this.sharingAgreements) {
      const dsa: Dsa = this.sharingAgreements[idx];
      this.region.sharingAgreements[dsa.uuid] = dsa.name;
    }

    // populate processing agreements
    this.region.processingAgreements = {};
    for (const idx in this.processingAgreements) {
      const dpa: Dpa = this.processingAgreements[idx];
      this.region.processingAgreements[dpa.uuid] = dpa.name;
    }

    this.regionService.saveRegion(this.region)
      .subscribe(saved => {
          this.region.uuid = saved;
          this.log.success('Region saved');
          if (close) { window.history.back(); }
        },
        error => this.log.error('The region could not be saved. Please try again.')
      );
  }

  close() {
    window.history.back();
  }

  private getRegionOrganisations() {

    this.regionService.getRegionOrganisations(this.region.uuid)
      .subscribe(
        result => this.organisations = result,
        error => this.log.error('The associated organisations could not be loaded. Please try again.')
      );
  }

  private getParentRegions() {

    this.regionService.getParentRegions(this.region.uuid, this.userId)
      .subscribe(
        result => this.parentRegions = result,
        error => this.log.error('The parent regions could not be loaded. Please try again.')
      );
  }

  private getChildRegions() {

    this.regionService.getChildRegions(this.region.uuid)
      .subscribe(
        result => this.childRegions = result,
        error => this.log.error('The child regions could not be loaded. Please try again.')
      );
  }

  private getSharingAgreements() {

    this.regionService.getSharingAgreements(this.region.uuid)
      .subscribe(
        result => {
          this.sharingAgreements = result;
        },
        error => this.log.error('The associated data sharing agreements could not be loaded. Please try again.')

      );
  }

  private getProcessingAgreements() {

    this.regionService.getProcessingAgreements(this.region.uuid)
      .subscribe(
        result => {
          this.processingAgreements = result;
        },
        error => this.log.error('The associated data processing agreements could not be loaded. Please try again.')

      );
  }

  private getOrganisationMarkers() {

    this.regionService.getOrganisationMarkers(this.region.uuid)
      .subscribe(
        result => {
          this.markers = result;
        },
            error => this.log.error('The map data could not be loaded. Please try again.')
      )
  }

  /*private editOrganisations() {

    OrganisationPickerComponent.open(this.$modal, this.organisations, 'organisations')
      .result.then(function (result: Organisation[]) {
      this.organisations = result;
    });
  }

  private editParentRegions() {

    RegionPickerComponent.open(this.$modal, this.parentRegions, this.region.uuid)
      .result.then(function (result: Region[]) {
      this.parentRegions = result;
    });
  }

  private editChildRegions() {

    RegionPickerComponent.open(this.$modal, this.childRegions, this.region.uuid)
      .result.then(function (result: Region[]) {
      this.childRegions = result;
    });
  }

  private editSharingAgreements() {

    DataSharingAgreementPickerComponent.open(this.$modal, this.sharingAgreements)
      .result.then(function (result: Dsa[]) {
      this.sharingAgreements = result;
    });
  }

  private editProcessingAgreements() {

    DataProcessingAgreementPickerComponent.open(this.$modal, this.processingAgreements)
      .result.then(function (result: Dpa[]) {
      this.processingAgreements = result;
    });
  }*/

  organisationClicked(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  regionClicked(item: Organisation) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  sharingAgreementClicked(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  processingAgreementClicked(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  deleteOrganisations() {
    console.log(this.orgTable.selection.selected);
  }

  deleteParentRegions() {
    console.log(this.parentRegionTable.selection.selected);
  }

  deleteChildRegions() {
    console.log(this.childRegionTable.selection.selected);
  }

  deleteDSAs() {
    console.log(this.dsaTable.selection.selected);
  }

  deleteDPAs() {
    console.log(this.dpaTable.selection.selected);
  }

}
