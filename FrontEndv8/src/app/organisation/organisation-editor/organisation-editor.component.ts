import {Component, OnInit, ViewChild} from '@angular/core';
import {OrganisationService} from '../organisation.service';
import {ActivatedRoute, Router} from '@angular/router';
import {DatePipe} from '@angular/common';
import {Region} from "../../region/models/Region";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {OrganisationType} from "../models/OrganisationType";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {Organisation} from "../models/Organisation";
import {Address} from "../models/Address";
import {MatDialog} from '@angular/material/dialog';
import {OrganisationPickerComponent} from "../organisation-picker/organisation-picker.component";
import {RegionPickerComponent} from "../../region/region-picker/region-picker.component";
import {OrganisationDialogComponent} from "../organisation-dialog/organisation-dialog.component";
import {AddressDialogComponent} from "../address-dialog/address-dialog.component";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";

@Component({
  selector: 'app-organisation-editor',
  templateUrl: './organisation-editor.component.html',
  styleUrls: ['./organisation-editor.component.css']
})
export class OrganisationEditorComponent implements OnInit {
  public accordionClass = 'accordionClass';
  private paramSubscriber: any;

  region: Region = <Region>{};
  organisation: Organisation = <Organisation>{};
  regions: Region[] = [];
  childOrganisations: Organisation[] = [];
  parentOrganisations: Organisation[] = [];
  services: Organisation[] = [];
  addresses: Address[] = [];
  dpaPublishing: Dpa[] = [];
  dsaPublishing: Dsa[] = [];
  dsaSubscribing: Dsa[] = [];
  organisationTypes: OrganisationType[];
  location: any;
  orgType = 'Organisation';
  allowEdit = false;
  superUser = false;
  userId: string;

  public activeProject: UserProject;

  orgDetailsToShow = new Organisation().getDisplayItems();
  regionDetailsToShow = new Region().getDisplayItems();
  dpaDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();
  addressDetailsToShow = new Address().getDisplayItems();

  systemSupplierSystems = [
    {num: 0, name: 'Not entered'},
    {num: 1, name: 'EMIS Web'},
    {num: 2, name: 'SystmOne'},
    {num: 3, name: 'Vision'},
    {num: 4, name: 'Adastra'},
    {num: 5, name: 'Cerner Millennium'},
    {num: 6, name: 'Rio'}
  ];

  systemSupplierSharingActivated = [
    {num: 0, name: 'No'},
    {num: 1, name: 'Yes'},
  ];

  @ViewChild('addressesTable', { static: false }) addressesTable: GenericTableComponent;
  @ViewChild('regionTable', { static: false }) regionTable: GenericTableComponent;
  @ViewChild('dpaTable', { static: false }) dpaTable: GenericTableComponent;
  @ViewChild('dsaPublishingTable', { static: false }) dsaPublishingTable: GenericTableComponent;
  @ViewChild('dsaSubscribingTable', { static: false }) dsaSubscribingTable: GenericTableComponent;
  @ViewChild('childOrgTable', { static: false }) childOrgTable: GenericTableComponent;
  @ViewChild('parentOrgTable', { static: false }) parentOrgTable: GenericTableComponent;
  @ViewChild('servicesTable', { static: false }) servicesTable: GenericTableComponent;

  constructor(private log: LoggerService,
              private organisationService: OrganisationService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService,
              private datePipe: DatePipe,
              public dialog: MatDialog) { }

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

    this.getOrganisationTypes();
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
      case 'addService':
        this.createService(itemUuid);
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  createService(uuid: string) {
    const vm = this;
    this.orgType = 'Service';
    this.organisation = {
      name: '',
      isService: 1,
      bulkImported : 0,
      bulkItemUpdated : 0
    } as Organisation;
  }

  createServiceFromOrg() {
    const vm = this;
    const parent: Organisation = (JSON.parse(JSON.stringify(this.organisation)));
    this.services = null;
    this.childOrganisations = null;
    this.regions = null;
    this.parentOrganisations = [];
    this.parentOrganisations.push(parent);
    this.organisation.uuid = null;
    this.organisation.isService = 1;
    this.orgType = 'Service';
    this.addresses = [];
  }

  create(uuid: string) {
    this.organisation = {
      name: '',
      isService: 0,
      bulkImported : 0,
      bulkItemUpdated : 0
    } as Organisation;
    this.addresses = [];

  }

  load(uuid: string) {
    const vm = this;
    this.organisationService.getOrganisation(uuid)
      .subscribe(result =>  {
          this.organisation = result;
          this.organisation.dateOfRegistration = this.datePipe.transform(this.organisation.dateOfRegistration,"yyyy-MM-dd");
          if (this.organisation.isService) {
            this.orgType = 'Service';
          } else { // only get these for organisations, not services
            this.getOrganisationRegions();
            this.getOrganisationAddresses();
            this.getChildOrganisations();
            this.getServices();
            this.getDPAsPublishingTo();
            this.getDSAsPublishingTo();
            this.getDSAsSubscribingTo();
          }
          this.getParentOrganisations();
        },
        error => this.log.error('The ' + this.orgType + ' could not be loaded. Please try again.')
      );
  }

  save(close: boolean) {
    const vm = this;
    // Populate organisations regions before save
    this.organisation.regions = {};
    for (const idx in this.regions) {
      const region: Region = this.regions[idx];
      this.organisation.regions[region.uuid] = region.name;
    }

    this.organisation.childOrganisations = {};
    for (const idx in this.childOrganisations) {
      const org: Organisation = this.childOrganisations[idx];
      this.organisation.childOrganisations[org.uuid] = org.name;
    }

    this.organisation.parentOrganisations = {};
    for (const idx in this.parentOrganisations) {
      const org: Organisation = this.parentOrganisations[idx];
      this.organisation.parentOrganisations[org.uuid] = org.name;
    }

    this.organisation.services = {};
    for (const idx in this.services) {
      const org: Organisation = this.services[idx];
      this.organisation.services[org.uuid] = org.name;
    }

    this.organisation.dpaPublishing = {};
    for (const idx in this.dpaPublishing) {
      const dpa: Dpa = this.dpaPublishing[idx];
      this.organisation.dpaPublishing[dpa.uuid] = dpa.name;
    }

    this.organisation.dsaPublishing = {};
    for (const idx in this.dsaPublishing) {
      const dsa: Dsa = this.dsaPublishing[idx];
      this.organisation.dsaPublishing[dsa.uuid] = dsa.name;
    }

    this.organisation.dsaSubscribing = {};
    for (const idx in this.dsaSubscribing) {
      const dsa: Dsa = this.dsaSubscribing[idx];
      this.organisation.dsaSubscribing[dsa.uuid] = dsa.name;
    }

    // Populate Addresses before save
    this.organisation.addresses = this.addresses;


    this.organisationService.saveOrganisation(this.organisation)
      .subscribe(saved => {
          this.organisation.uuid = saved;
          this.log.success('Organisation saved successfully.');
          if (close) { window.history.back(); }
        },
        error => this.log.error('The organisation could not be saved. Please try again.')
      );
  }

  close() {
    window.history.back();
  }

  addressClicked(address : Address) {
    let index = this.addresses.indexOf(address);
    const dialogRef = this.dialog.open(AddressDialogComponent, {
      width: '800px',
      data: {address: address},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (index > -1) {
          this.addresses[index] = address;
        } else {
          this.addresses.push(address);
        }
        this.addressesTable.updateRows();
        this.clearMappings();
        this.organisation.addresses = this.addresses;
        this.updateMappings('Address');
      }
    });
  }

  updateMappings(type : string) {
    this.organisationService.updateMappings(this.organisation)
      .subscribe(saved => {
          this.organisation.uuid = saved;
          this.log.success(type + ' saved successfully.');
        },
        error => this.log.error(type  + ' could not be saved. Please try again.')
      );
  }

  addAddress() {
    const address: Address = <Address>{};
    address.uuid = null;
    address.organisationUuid = this.organisation.uuid;
    address.buildingName = '';
    address.numberAndStreet = '';
    address.locality = '';
    address.city = '';
    address.county = '';
    address.postcode = '';
    address.lat = null;
    address.lng = null;
    address.geolocationReprocess = null;
    this.addressClicked(address);
  }

  deleteAddress() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete address', 'Are you sure you want to delete address(es)?',
      'Delete address', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.addressesTable.selection.selected.length; i++) {
              let address = this.addressesTable.selection.selected[i];
              this.addresses.forEach( (item, index) => {
                if(item === address) this.addresses.splice(index,1);
              });
            }
            this.addressesTable.updateRows();
            this.clearMappings();
            this.organisation.addresses = this.addresses;
            this.updateMappings('Address');
          } else {
            this.log.success('Delete cancelled.')
          }
        },
      );
  }

  clearMappings() {
    this.organisation.regions = null;
    this.organisation.childOrganisations = null;
    this.organisation.parentOrganisations = null;
    this.organisation.services = null;
    this.organisation.dpaPublishing = null;
    this.organisation.dsaPublishing = null;
    this.organisation.dsaSubscribing = null;
    this.organisation.addresses = null;
  }

  addRegion() {
    const dialogRef = this.dialog.open(RegionPickerComponent, {
      width: '800px',
      data: { uuid: '', limit: 0, userId : this.activeProject.userId }
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let region of result) {
        if (!this.regions.some(x => x.uuid === region.uuid)) {
          this.regions.push(region);
          this.regionTable.updateRows();
        }
      }
      this.clearMappings();
      this.organisation.regions = {};
      for (const idx in this.regions) {
        const region: Region = this.regions[idx];
        this.organisation.regions[region.uuid] = region.name;
      }
      this.updateMappings('Regions');
    })
  }

  regionClicked(item: Region) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  deleteRegion() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove region', 'Are you sure you want to remove region(s)?',
      'Remove region', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.regionTable.selection.selected.length; i++) {
              let region = this.regionTable.selection.selected[i];
              this.regions.forEach( (item, index) => {
                if(item === region) this.regions.splice(index,1);
              });
            }
            this.regionTable.updateRows();
            this.clearMappings();
            this.organisation.regions = {};
            for (const idx in this.regions) {
              const region: Region = this.regions[idx];
              this.organisation.regions[region.uuid] = region.name;
            }
            this.updateMappings('Regions');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  editOrg(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  deleteChildOrganisations() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove Organisation', 'Are you sure you want to remove child organisation(s)?',
      'Remove Organisation', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.childOrgTable.selection.selected.length; i++) {
              let org = this.childOrgTable.selection.selected[i];
              this.childOrganisations.forEach( (item, index) => {
                if(item === org) this.childOrganisations.splice(index,1);
              });
            }
            this.childOrgTable.updateRows();
            this.clearMappings();
            this.organisation.childOrganisations = {};
            for (const idx in this.childOrganisations) {
              const org: Organisation = this.childOrganisations[idx];
              this.organisation.childOrganisations[org.uuid] = org.name;
            }
            this.updateMappings('Child Organisations');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addChildOrganisations() {
    const dialogRef = this.dialog.open(OrganisationPickerComponent, {
        width: '800px',
        data: { searchType: 'organisation', uuid: '', regionUUID: '', dsaUUID: '', existingOrgs: this.childOrganisations }
      })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let org of result) {
        if (!this.childOrganisations.some(x => x.uuid === org.uuid)) {
          this.childOrganisations.push(org);
        }
      }
      this.childOrgTable.updateRows();
      this.clearMappings();
      this.organisation.childOrganisations = {};
      for (const idx in this.childOrganisations) {
        const org: Organisation = this.childOrganisations[idx];
        this.organisation.childOrganisations[org.uuid] = org.name;
      }
      this.updateMappings('Child Organisations');
    })
  }

  deleteParentOrganisations() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove Organisation', 'Are you sure you want to remove parent organisation(s)?',
      'Remove Organisation', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.parentOrgTable.selection.selected.length; i++) {
              let org = this.parentOrgTable.selection.selected[i];
              this.parentOrganisations.forEach( (item, index) => {
                if(item === org) this.parentOrganisations.splice(index,1);
              });
            }
            this.parentOrgTable.updateRows();
            this.clearMappings();
            this.organisation.parentOrganisations = {};
            for (const idx in this.parentOrganisations) {
              const org: Organisation = this.parentOrganisations[idx];
              this.organisation.parentOrganisations[org.uuid] = org.name;
            }
            this.updateMappings('Parent Organisations');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addParentOrganisations() {
    const dialogRef = this.dialog.open(OrganisationPickerComponent, {
      width: '800px',
      data: { searchType: 'organisation', uuid: '', regionUUID: '', dsaUUID: '', existingOrgs: this.parentOrganisations }
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let org of result) {
        if (!this.parentOrganisations.some(x => x.uuid === org.uuid)) {
          this.parentOrganisations.push(org);
        }
      }
      this.parentOrgTable.updateRows();
      this.clearMappings();
      this.organisation.parentOrganisations = {};
      for (const idx in this.parentOrganisations) {
        const org: Organisation = this.parentOrganisations[idx];
        this.organisation.parentOrganisations[org.uuid] = org.name;
      }
      this.updateMappings('Parent Organisations');
    })
  }

  deleteServices() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove Service', 'Are you sure you want to remove service(s)?',
      'Remove Service', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.servicesTable.selection.selected.length; i++) {
              let org = this.servicesTable.selection.selected[i];
              this.services.forEach( (item, index) => {
                if(item === org) this.services.splice(index,1);
              });
            }
            this.servicesTable.updateRows();
            this.clearMappings();
            this.organisation.services = {};
            for (const idx in this.services) {
              const org: Organisation = this.services[idx];
              this.organisation.services[org.uuid] = org.name;
            }
            this.updateMappings('Services');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addServices() {
    const dialogRef = this.dialog.open(OrganisationPickerComponent, {
      width: '800px',
      data: { searchType: 'organisation', uuid: '', regionUUID: '', dsaUUID: '', existingOrgs: this.services }
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let org of result) {
        if (!this.services.some(x => x.uuid === org.uuid)) {
          this.services.push(org);
        }
      }
      this.servicesTable.updateRows();
      this.clearMappings();
      this.organisation.services = {};
      for (const idx in this.services) {
        const org: Organisation = this.services[idx];
        this.organisation.services[org.uuid] = org.name;
      }
      this.updateMappings('Services');
    })
  }

  private getOrganisationRegions() {
    const vm = this;
    this.organisationService.getOrganisationRegions(this.organisation.uuid, this.userId)
      .subscribe(
        result => this.regions = result,
        error => this.log.error('The associated regions could not be loaded. Please try again.')
      );
  }

  private getOrganisationAddresses() {
    const vm = this;
    this.organisationService.getOrganisationAddresses(this.organisation.uuid)
      .subscribe(
        result => this.addresses = result,
        error => this.log.error('The address details could not be loaded. Please try again.')
      );
  }

  private getChildOrganisations() {
    const vm = this;
    this.organisationService.getChildOrganisations(this.organisation.uuid)
      .subscribe(
        result => this.childOrganisations = result,
        error => this.log.error('The child organisations could not be loaded. Please try again.')
      );
  }

  private getParentOrganisations() {
    this.organisationService.getParentOrganisations(this.organisation.uuid, this.organisation.isService)
      .subscribe(
        result => this.parentOrganisations = result,
        error => this.log.error('The parent organisations could not be loaded. Please try again.')
      );
  }

  private getServices() {
    const vm = this;
    this.organisationService.getServices(this.organisation.uuid)
      .subscribe(
        result => this.services = result,
        error => this.log.error('The associated services could not be loaded. Please try again.')
      );
  }

  private getOrganisationTypes() {
    const vm = this;
    this.organisationService.getOrganisationTypes()
      .subscribe(
        result => {this.organisationTypes = result;
        },
        error => this.log.error('The organisation types could not be loaded. Please try again.')
      );
  }

  private getDPAsPublishingTo() {
    const vm = this;
    this.organisationService.getDPAPublishing(this.organisation.uuid)
      .subscribe(
        result => this.dpaPublishing = result,
        error => this.log.error('The associated publishing data processing agreements could not be loaded. Please try again.')
      );
  }

  private getDSAsPublishingTo() {
    const vm = this;
    this.organisationService.getDSAPublishing(this.organisation.uuid)
      .subscribe(
        result => this.dsaPublishing = result,
        error => this.log.error('The associated publishing data sharing agreements could not be loaded. Please try again.')
      );
  }

  private getDSAsSubscribingTo() {
    const vm = this;
    this.organisationService.getDSASubscribing(this.organisation.uuid)
      .subscribe(
        result => this.dsaSubscribing = result,
        error => this.log.error('The associated subscribing data sharing agreements could not be loaded. Please try again.')
      );
  }

  editOrganisation() {
    const dialogRef = this.dialog.open(OrganisationDialogComponent, {
      width: '1200px',
      data: {mode: 'edit', uuid: this.organisation.uuid, orgType: this.orgType},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.organisation = result;
        this.log.success(this.orgType + ' saved.');
      }
    });
  }

  editDpa(dpa: Dpa) {
    this.router.navigate(['/dpa', dpa.uuid, 'edit']);
  }

  deleteDPAs() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove DPA', 'Are you sure you want to remove DPA(s)?',
      'Remove DPA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dpaTable.selection.selected.length; i++) {
              let org = this.dpaTable.selection.selected[i];
              this.dpaPublishing.forEach( (item, index) => {
                if(item === org) this.dpaPublishing.splice(index,1);
              });
            }
            this.dpaTable.updateRows();
            this.clearMappings();
            this.organisation.dpaPublishing = {};
            for (const idx in this.dpaPublishing) {
              const dpa: Dpa = this.dpaPublishing[idx];
              this.organisation.dpaPublishing[dpa.uuid] = dpa.name;
            }
            this.updateMappings('DPA');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addDPAs() {
    const dialogRef = this.dialog.open(DataProcessingAgreementPickerComponent, {
      width: '800px',
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dpa of result) {
        if (!this.dpaPublishing.some(x => x.uuid === dpa.uuid)) {
          this.dpaPublishing.push(dpa);
        }
      }
      this.dpaTable.updateRows();
      this.clearMappings();
      this.organisation.dpaPublishing = {};
      for (const idx in this.dpaPublishing) {
        const dpa: Dpa = this.dpaPublishing[idx];
        this.organisation.dpaPublishing[dpa.uuid] = dpa.name;
      }
      this.updateMappings('DPA');
    })
  }

  editDsa(dsa: Dsa) {
    this.router.navigate(['/dsa', dsa.uuid, 'edit']);
  }

  addDSAPublishing() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      width: '800px',
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dsa of result) {
        if (!this.dsaPublishing.some(x => x.uuid === dsa.uuid)) {
          this.dsaPublishing.push(dsa);
        }
      }
      this.dsaPublishingTable.updateRows();
      this.clearMappings();
      this.organisation.dsaPublishing = {};
      for (const idx in this.dsaPublishing) {
        const dsa: Dsa = this.dsaPublishing[idx];
        this.organisation.dsaPublishing[dsa.uuid] = dsa.name;
      }
      this.updateMappings('DSA Publishing');
    })
  }

  deleteDSAPublishing() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove DSA', 'Are you sure you want to remove DSA(s)?',
      'Remove DSA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dsaPublishingTable.selection.selected.length; i++) {
              let org = this.dsaPublishingTable.selection.selected[i];
              this.dsaPublishing.forEach( (item, index) => {
                if(item === org) this.dsaPublishing.splice(index,1);
              });
            }
            this.dsaPublishingTable.updateRows();
            this.clearMappings();
            this.organisation.dsaPublishing = {};
            for (const idx in this.dsaPublishing) {
              const dsa: Dsa = this.dsaPublishing[idx];
              this.organisation.dsaPublishing[dsa.uuid] = dsa.name;
            }
            this.updateMappings('DSA Publishing');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addDSASubscribing() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      width: '800px',
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dsa of result) {
        if (!this.dsaSubscribing.some(x => x.uuid === dsa.uuid)) {
          this.dsaSubscribing.push(dsa);
        }
      }
      this.dsaSubscribingTable.updateRows();
      this.clearMappings();
      this.organisation.dsaSubscribing = {};
      for (const idx in this.dsaSubscribing) {
        const dsa: Dsa = this.dsaSubscribing[idx];
        this.organisation.dsaSubscribing[dsa.uuid] = dsa.name;
      }
      this.updateMappings('DSA Subscribing');
    })
  }

  deleteDSASubscribing() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove DSA', 'Are you sure you want to remove DSA(s)?',
      'Remove DSA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dsaSubscribingTable.selection.selected.length; i++) {
              let org = this.dsaSubscribingTable.selection.selected[i];
              this.dsaSubscribing.forEach( (item, index) => {
                if(item === org) this.dsaSubscribing.splice(index,1);
              });
            }
            this.dsaSubscribingTable.updateRows();
            this.clearMappings();
            this.organisation.dsaSubscribing = {};
            for (const idx in this.dsaSubscribing) {
              const dsa: Dsa = this.dsaSubscribing[idx];
              this.organisation.dsaSubscribing[dsa.uuid] = dsa.name;
            }
            this.updateMappings('DSA Subscribing');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }
}
