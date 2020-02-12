import {Component, OnInit, QueryList, ViewChild} from '@angular/core';
import {RegionService} from '../region.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Region} from "../models/Region";
import {Organisation} from "../../organisation/models/Organisation";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Marker} from "../models/Marker";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {MatDialog} from "@angular/material/dialog";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {RegionPickerComponent} from "../region-picker/region-picker.component";
import {OrganisationPickerComponent} from "../../organisation/organisation-picker/organisation-picker.component";
import {RegionDialogComponent} from "../region-dialog/region-dialog.component";

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
              private userManagerService: UserManagerService,
              public dialog: MatDialog) {}

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
      name: ''
    } as Region;
  }

  load(uuid: string) {

    this.regionService.getRegion(uuid)
      .subscribe(result => {
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
    this.regionService.saveRegion(this.region)
      .subscribe(saved => {
          this.region.uuid = saved;
          this.log.success('Region saved');
          if (close) {
            window.history.back();
          }
        },
        error => this.log.error('The region could not be saved. Please try again.')
      );
  }

  editRegion() {
    const dialogRef = this.dialog.open(RegionDialogComponent, {
      width: '80vw',
      data: {mode: 'edit', uuid: this.region.uuid},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.region = result;
        this.log.success('Region saved.');
      }
    });
  }

  close() {
    window.history.back();
  }

  private getRegionOrganisations() {
    this.regionService.getRegionOrganisations(this.region.uuid)
      .subscribe(
        result => {
          this.organisations = result;
          this.orgTable.updateRows();
        },
        error => this.log.error('The associated organisations could not be loaded. Please try again.')
      );
  }

  private getParentRegions() {
    this.regionService.getParentRegions(this.region.uuid, this.userId)
      .subscribe(
        result => {
          this.parentRegions = result;
          this.parentRegionTable.updateRows();
        },
        error => this.log.error('The parent regions could not be loaded. Please try again.')
      );
  }

  private getChildRegions() {
    this.regionService.getChildRegions(this.region.uuid)
      .subscribe(
        result => {
          this.childRegions = result;
          this.childRegionTable.updateRows();
        },
        error => this.log.error('The child regions could not be loaded. Please try again.')
      );
  }

  private getSharingAgreements() {
    this.regionService.getSharingAgreements(this.region.uuid)
      .subscribe(
        result => {
          this.sharingAgreements = result;
          this.dsaTable.updateRows();
        },
        error => this.log.error('The associated data sharing agreements could not be loaded. Please try again.')
      );
  }

  private getProcessingAgreements() {
    this.regionService.getProcessingAgreements(this.region.uuid)
      .subscribe(
        result => {
          this.processingAgreements = result;
          this.dpaTable.updateRows();
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

  addDSAs() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      width: '80vw',
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dsa of result) {
        if (!this.sharingAgreements.some(x => x.uuid === dsa.uuid)) {
          this.sharingAgreements.push(dsa);
        }
      }
      this.clearMappings();
      this.region.sharingAgreements = {};
      for (const idx in this.sharingAgreements) {
        const dsa: Dsa = this.sharingAgreements[idx];
        this.region.sharingAgreements[dsa.uuid] = dsa.name;
      }
      this.updateMappings('DSA');
    })
  }

  addDPAs() {
    const dialogRef = this.dialog.open(DataProcessingAgreementPickerComponent, {
      width: '80vw',
      data: {fromRegion: true},
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dpa of result) {
        if (!this.processingAgreements.some(x => x.uuid === dpa.uuid)) {
          this.processingAgreements.push(dpa);
        }
      }
      this.clearMappings();
      this.region.processingAgreements = {};
      for (const idx in this.processingAgreements) {
        const dpa: Dpa = this.processingAgreements[idx];
        this.region.processingAgreements[dpa.uuid] = dpa.name;
      }
      this.updateMappings('DPA');
    })
  }

  addRegions(isParent: boolean) {
    let type = '';
    if (isParent) {
      type = 'parent';
    } else {
      type = 'child';
    }
    const dialogRef = this.dialog.open(RegionPickerComponent, {
      width: '80vw',
      data: {uuid: '', limit: 0, userId: '', type: type}
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let region of result) {
        if (isParent) {
          if (!this.parentRegions.some(x => x.uuid === region.uuid)) {
            this.parentRegions.push(region);
          }
        } else {
          if (!this.childRegions.some(x => x.uuid === region.uuid)) {
            this.childRegions.push(region);
          }
        }
      }
      this.clearMappings();
      if (isParent) {
        this.region.parentRegions = {};
        for (const idx in this.parentRegions) {
          const reg: Region = this.parentRegions[idx];
          this.region.parentRegions[reg.uuid] = reg.name;
        }
        this.updateMappings('Parent regions');
      } else {
        this.region.childRegions = {};
        for (const idx in this.childRegions) {
          const reg: Region = this.childRegions[idx];
          this.region.childRegions[reg.uuid] = reg.name;
        }
        this.updateMappings('Child regions');
      }
    })
  }

  addOrganisation() {
    const dialogRef = this.dialog.open(OrganisationPickerComponent, {
      width: '80vw',
      data: {searchType: 'organisation', uuid: '', regionUUID: '', dsaUUID: '', existingOrgs: this.organisations}
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let org of result) {
        if (!this.organisations.some(x => x.uuid === org.uuid)) {
          this.organisations.push(org);
        }
      }
      this.clearMappings();
      this.region.organisations = {};
      for (const idx in this.organisations) {
        const org: Organisation = this.organisations[idx];
        this.region.organisations[org.uuid] = org.name;
      }
      this.updateMappings('Organisations');
    })
  }

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
    MessageBoxDialogComponent.open(this.dialog, 'Remove organisations', 'Are you sure you want to remove organisations?',
      'Remove organisations', 'Cancel')
      .subscribe(
        (result) => {
          if (result) {
            for (var i = 0; i < this.orgTable.selection.selected.length; i++) {
              let org = this.orgTable.selection.selected[i];
              this.organisations.forEach((item, index) => {
                if (item === org) this.organisations.splice(index, 1);
              });
            }
            this.clearMappings();
            this.region.organisations = {};
            for (const idx in this.organisations) {
              const org: Organisation = this.organisations[idx];
              this.region.organisations[org.uuid] = org.name;
            }
            this.updateMappings('Organisations');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteParentRegions() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove parent regions', 'Are you sure you want to remove parent regions?',
      'Remove regions', 'Cancel')
      .subscribe(
        (result) => {
          if (result) {
            for (var i = 0; i < this.parentRegionTable.selection.selected.length; i++) {
              let org = this.parentRegionTable.selection.selected[i];
              this.parentRegions.forEach((item, index) => {
                if (item === org) this.parentRegions.splice(index, 1);
              });
            }
            this.clearMappings();
            this.region.parentRegions = {};
            for (const idx in this.parentRegions) {
              const reg: Region = this.parentRegions[idx];
              this.region.parentRegions[reg.uuid] = reg.name;
            }
            this.updateMappings('Parent regions');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteChildRegions() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove child regions', 'Are you sure you want to remove child regions?',
      'Remove regions', 'Cancel')
      .subscribe(
        (result) => {
          if (result) {
            for (var i = 0; i < this.childRegionTable.selection.selected.length; i++) {
              let org = this.childRegionTable.selection.selected[i];
              this.childRegions.forEach((item, index) => {
                if (item === org) this.childRegions.splice(index, 1);
              });
            }
            this.clearMappings();
            this.region.childRegions = {};
            for (const idx in this.childRegions) {
              const reg: Region = this.childRegions[idx];
              this.region.childRegions[reg.uuid] = reg.name;
            }
            this.updateMappings('Child regions');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteDSAs() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove data sharing agreements', 'Are you sure you want to remove data sharing agreements',
      'Delete agreements', 'Cancel')
      .subscribe(
        (result) => {
          if (result) {
            for (var i = 0; i < this.dsaTable.selection.selected.length; i++) {
              let org = this.dsaTable.selection.selected[i];
              this.sharingAgreements.forEach((item, index) => {
                if (item === org) this.sharingAgreements.splice(index, 1);
              });
            }
            this.clearMappings();
            this.region.sharingAgreements = {};
            for (const idx in this.sharingAgreements) {
              const dsa: Dsa = this.sharingAgreements[idx];
              this.region.sharingAgreements[dsa.uuid] = dsa.name;
            }
            this.updateMappings('DSA');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteDPAs() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove data processing agreements', 'Are you sure you want to remove data processing agreements?',
      'Remove agreements', 'Cancel')
      .subscribe(
        (result) => {
          if (result) {
            for (var i = 0; i < this.dpaTable.selection.selected.length; i++) {
              let org = this.dpaTable.selection.selected[i];
              this.processingAgreements.forEach((item, index) => {
                if (item === org) this.processingAgreements.splice(index, 1);
              });
            }
            this.clearMappings();
            this.region.processingAgreements = {};
            for (const idx in this.processingAgreements) {
              const dpa: Dpa = this.processingAgreements[idx];
              this.region.processingAgreements[dpa.uuid] = dpa.name;
            }
            this.updateMappings('DPA');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  updateMappings(type: string) {
    this.regionService.updateMappings(this.region)
      .subscribe(saved => {
          this.region.uuid = saved;
          if (type == 'DSA') {
            this.getSharingAgreements();
          } else if (type == 'DPA') {
            this.getProcessingAgreements();
          } else if (type == 'Parent regions') {
            this.getParentRegions();
          } else if (type == 'Child regions') {
            this.getChildRegions();
          } else if (type == 'Organisations') {
            this.getRegionOrganisations();
          }
          this.log.success(type + ' updated successfully.');
        },
        error => this.log.error('The region could not be saved. Please try again.')
      );
  }

  clearMappings() {
    this.region.organisations = null;
    this.region.parentRegions = null;
    this.region.childRegions = null;
    this.region.sharingAgreements = null;
    this.region.processingAgreements = null;
  }
}
