import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {Organisation} from '../models/Organisation';
import {Address} from '../models/Address';
import {LoggerService, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {RegionPickerComponent} from '../../region/region-picker/region-picker.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {OrganisationService} from '../organisation.service';
import {Region} from '../../region/models/Region';
import {OrganisationPickerComponent} from '../organisation-picker/organisation-picker.component';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastsManager} from 'ng2-toastr';
import {Dpa} from '../../data-processing-agreement/models/Dpa';
import {Dsa} from '../../data-sharing-agreement/models/Dsa';
import {DataProcessingAgreementPickerComponent} from '../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component';
import {DataSharingAgreementPickerComponent} from '../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component';
import {OrganisationType} from '../models/OrganisationType';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

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
  regions: Region[];
  childOrganisations: Organisation[];
  parentOrganisations: Organisation[];
  services: Organisation[];
  addresses: Address[];
  dpaPublishing: Dpa[];
  dsaPublishing: Dsa[];
  dsaSubscribing: Dsa[];
  organisationTypes: OrganisationType[];
  location: any;
  orgType = 'Organisation';
  allowEdit = false;

  public activeProject: UserProject;

  orgDetailsToShow = new Organisation().getDisplayItems();
  regionDetailsToShow = new Region().getDisplayItems();
  dpaDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private organisationService: OrganisationService,
              private securityService: SecurityService,
              private router: Router,
              private route: ActivatedRoute,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.getOrganisationTypes();

    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode'], params['id']);
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
    vm.orgType = 'Service';
    this.organisation = {
      name: '',
      isService: 1,
      bulkImported : 0,
      bulkItemUpdated : 0
    } as Organisation;
  }

  createServiceFromOrg() {
    const vm = this;
    const parent: Organisation = (JSON.parse(JSON.stringify(vm.organisation)));
    vm.services = null;
    vm.childOrganisations = null;
    vm.regions = null;
    vm.parentOrganisations = [];
    vm.parentOrganisations.push(parent);
    vm.organisation.uuid = null;
    vm.organisation.isService = 1;
    vm.orgType = 'Service';
    vm.addresses = [];
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
    vm.organisationService.getOrganisation(uuid)
      .subscribe(result =>  {
          vm.organisation = result;
          if (vm.organisation.isService) {
            vm.orgType = 'Service';
          } else { // only get these for organisations, not services
            vm.getOrganisationRegions();
            vm.getOrganisationAddresses();
            vm.getChildOrganisations();
            vm.getServices();
            vm.getDPAsPublishingTo();
            vm.getDSAsPublishingTo();
            vm.getDSAsSubscribingTo();
          }
          vm.getParentOrganisations();
        },
        error => vm.log.error('The organisation could not be loaded. Please try again.', error, 'Load organisation')
      );
  }

  save(close: boolean) {
    const vm = this;
    // Populate organisations regions before save
    vm.organisation.regions = {};
    for (const idx in this.regions) {
      const region: Region = this.regions[idx];
      this.organisation.regions[region.uuid] = region.name;
    }

    vm.organisation.childOrganisations = {};
    for (const idx in this.childOrganisations) {
      const org: Organisation = this.childOrganisations[idx];
      this.organisation.childOrganisations[org.uuid] = org.name;
    }

    vm.organisation.parentOrganisations = {};
    for (const idx in this.parentOrganisations) {
      const org: Organisation = this.parentOrganisations[idx];
      this.organisation.parentOrganisations[org.uuid] = org.name;
    }

    vm.organisation.services = {};
    for (const idx in this.services) {
      const org: Organisation = this.services[idx];
      this.organisation.services[org.uuid] = org.name;
    }

    vm.organisation.dpaPublishing = {};
    for (const idx in this.dpaPublishing) {
      const dpa: Dpa = this.dpaPublishing[idx];
      this.organisation.dpaPublishing[dpa.uuid] = dpa.name;
    }

    vm.organisation.dsaPublishing = {};
    for (const idx in this.dsaPublishing) {
      const dsa: Dsa = this.dsaPublishing[idx];
      this.organisation.dsaPublishing[dsa.uuid] = dsa.name;
    }

    vm.organisation.dsaSubscribing = {};
    for (const idx in this.dsaSubscribing) {
      const dsa: Dsa = this.dsaSubscribing[idx];
      this.organisation.dsaSubscribing[dsa.uuid] = dsa.name;
    }

    // Populate Addresses before save
    vm.organisation.addresses = this.addresses;


    vm.organisationService.saveOrganisation(vm.organisation)
      .subscribe(saved => {
          vm.organisation.uuid = saved;
          vm.log.success('Organisation saved successfully.', vm.organisation, 'Save organisation');
          if (close) { this.router.navigate(['/overview']); }
        },
        error => vm.log.error('The organisation could not be saved. Please try again.', error, 'Save organisation')
      );
  }

  close() {
    this.router.navigate(['/overview']);
  }

  addAddress() {
    const vm = this;
    const address: Address = <Address>{};
    address.uuid = null;
    address.organisationUuid = vm.organisation.uuid;
    address.buildingName = '';
    address.numberAndStreet = '';
    address.locality = '';
    address.city = '';
    address.county = '';
    address.postcode = '';
    address.lat = null;
    address.lng = null;
    address.geolocationReprocess = null;
    vm.addresses.push(address);

  }

  private editRegions() {
    const vm = this;
    RegionPickerComponent.open(vm.$modal, vm.regions)
      .result.then(function (result: Region[]) {
      vm.regions = result;
    });
  }

  private editChildOrganisations() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.childOrganisations, 'organisation', vm.organisation.uuid )
      .result.then(function (result: Organisation[]) {
      vm.childOrganisations = result;
    });
  }

  private editParentOrganisations() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.parentOrganisations, 'organisation', vm.organisation.uuid )
      .result.then(function (result: Organisation[]) {
      vm.parentOrganisations = result;
    });
  }

  private editServices() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.services, 'services', vm.organisation.uuid )
      .result.then(function (result: Organisation[]) {
      vm.services = result;
    });
  }

  private editDPAPublishing() {
    const vm = this;
    DataProcessingAgreementPickerComponent.open(vm.$modal, vm.dpaPublishing)
      .result.then(function (result: Dpa[]) {
      vm.dpaPublishing = result;
    });
  }

  private editDSAPublishing() {
    const vm = this;
    DataSharingAgreementPickerComponent.open(vm.$modal, vm.dsaPublishing)
      .result.then(function (result: Dsa[]) {
      vm.dsaPublishing = result;
    });
  }

  private editDSASubscribing() {
    const vm = this;
    DataSharingAgreementPickerComponent.open(vm.$modal, vm.dsaSubscribing)
      .result.then(function (result: Dsa[]) {
      vm.dsaSubscribing = result;
    });
  }

  private getOrganisationRegions() {
    const vm = this;
    vm.organisationService.getOrganisationRegions(vm.organisation.uuid)
      .subscribe(
        result => vm.regions = result,
        error => vm.log.error('The associated regions could not be loaded. Please try again.', error, 'Load organisation regions')
      );
  }

  private getOrganisationAddresses() {
    const vm = this;
    vm.organisationService.getOrganisationAddresses(vm.organisation.uuid)
      .subscribe(
        result => vm.addresses = result,
        error => vm.log.error('The address details could not be loaded. Please try again.', error, 'Load organisation addresses')
      );
  }

  private getChildOrganisations() {
    const vm = this;
    vm.organisationService.getChildOrganisations(vm.organisation.uuid)
      .subscribe(
        result => vm.childOrganisations = result,
        error => vm.log.error('The child organisations could not be loaded. Please try again.', error, 'Load child organisations')
      );
  }

  private getParentOrganisations() {
    const vm = this;
    vm.organisationService.getParentOrganisations(vm.organisation.uuid, vm.organisation.isService)
      .subscribe(
        result => vm.parentOrganisations = result,
        error => vm.log.error('The parent organisations could not be loaded. Please try again.', error, 'Load parent organisations')
      );
  }

  private getServices() {
    const vm = this;
    vm.organisationService.getServices(vm.organisation.uuid)
      .subscribe(
        result => vm.services = result,
        error => vm.log.error('The associated services could not be loaded. Please try again.', error, 'Load services')
      );
  }

  private getOrganisationTypes() {
    const vm = this;
    vm.organisationService.getOrganisationTypes()
      .subscribe(
        result => {vm.organisationTypes = result;
        },
        error => vm.log.error('The organisation types could not be loaded. Please try again.', error, 'Load organisation types')
      );
  }

  private getDPAsPublishingTo() {
    const vm = this;
    vm.organisationService.getDPAPublishing(vm.organisation.uuid)
      .subscribe(
        result => vm.dpaPublishing = result,
        error => vm.log.error('The associated publishing data processing agreements could not be loaded. Please try again.', error, 'Load publishing data processing agreements')
      );
  }

  private getDSAsPublishingTo() {
    const vm = this;
    vm.organisationService.getDSAPublishing(vm.organisation.uuid)
      .subscribe(
        result => vm.dsaPublishing = result,
        error => vm.log.error('The associated publishing data sharing agreements could not be loaded. Please try again.', error, 'Load publishing data sharing agreements')
      );
  }

  private getDSAsSubscribingTo() {
    const vm = this;
    vm.organisationService.getDSASubscribing(vm.organisation.uuid)
      .subscribe(
        result => vm.dsaSubscribing = result,
        error => vm.log.error('The associated subscribing data sharing agreements could not be loaded. Please try again.', error, 'Load subscribing data sharing agreements')
      );
  }

  deleteAddress(address: Address) {
    console.log('deleting');
    console.log(address);
    const index = this.addresses.findIndex(a => a.uuid === address.uuid);
    console.log(index);
    if (index > -1) {
      this.addresses.splice(index, 1);
    }
  }

  editOrganisation(item: Organisation) {
    this.router.navigate(['/organisation', item.uuid, 'edit']);
  }

  editRegion(item: Organisation) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }
}
