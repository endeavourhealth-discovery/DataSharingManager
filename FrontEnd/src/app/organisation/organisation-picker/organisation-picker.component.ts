import { Component, OnInit, Input } from '@angular/core';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {LoggerService} from 'eds-angular4';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {RegionService} from "../../region/region.service";
import {DataSharingAgreementService} from "../../data-sharing-agreement/data-sharing-agreement.service";

@Component({
  selector: 'app-organisation-picker',
  templateUrl: './organisation-picker.component.html',
  styleUrls: ['./organisation-picker.component.css']
})
export class OrganisationPickerComponent implements OnInit {
  @Input() resultData: Organisation[];
  searchData: string;
  availableOrgs: Organisation[];
  searchResults: Organisation[];
  multipleSearchResults: Organisation[];
  multipleSearchMissing: string[];
  multipleAddedCount = 0;
  showMultipleMessage = false;
  searchType: string;
  uuid: string;
  regionUUID: string;
  dsaUUID: string;
  odsCodes: string;

  public static open(modalService: NgbModal, organisations: Organisation[], searchType: string, uuid: string = '',
                     regionUUID: string = '', dsaUUID : string = '') {
    const modalRef = modalService.open(OrganisationPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], organisations);
    modalRef.componentInstance.searchType = searchType;
    modalRef.componentInstance.regionUUID = regionUUID;
    modalRef.componentInstance.dsaUUID = dsaUUID;

    return modalRef;
  }

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              private organisationService: OrganisationService,
              private regionService: RegionService,
              private dsaService: DataSharingAgreementService) { }

  ngOnInit() {
    const vm = this;
    if (vm.regionUUID != '') {
      this.getRegionOrganisations(vm.regionUUID);
    } else if (vm.dsaUUID != '') {
      if (vm.searchType == 'publisher') {
        this.getDSAPublishers(vm.dsaUUID);
      } else if (vm.searchType == 'subscriber') {
        this.getDSASubscribers(vm.dsaUUID);
      }
    }
  }

  search($event: KeyboardEvent) {
    const vm = this;
    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    console.log('gete');
    if (vm.searchData.length < 3) {
      return;
    }

    if (vm.regionUUID != '') {
      vm.organisationService.searchOrganisationsInParentRegion(vm.regionUUID, vm.searchData)
        .subscribe(
          (result) => vm.searchResults = result.filter(function (x) {
            return x.uuid != vm.uuid;
          }),
          (error) => vm.log.error(error)
        );
    } else if (vm.dsaUUID != '' && vm.searchType == 'publisher') {
      vm.organisationService.searchPublishersInDSA(vm.dsaUUID, vm.searchData)
        .subscribe(
          (result) => vm.searchResults = result.filter(function (x) {
            return x.uuid != vm.uuid;
          }),
          (error) => vm.log.error(error)
        );
    } else if (vm.dsaUUID != '' && vm.searchType == 'subscriber') {
      vm.organisationService.searchSubscribersInDSA(vm.dsaUUID, vm.searchData)
        .subscribe(
          (result) => vm.searchResults = result.filter(function (x) {
            return x.uuid != vm.uuid;
          }),
          (error) => vm.log.error(error)
        );
    } else {
      console.log('searching');
      vm.organisationService.search(vm.searchData, vm.searchType)
        .subscribe(
          (result) => {console.log(result); vm.searchResults = result.filter(function (x) {
            return x.uuid != vm.uuid;
          })},
          (error) => vm.log.error(error)
        );
    }
  }

  searchMultiple() {
    const vm = this;
    vm.showMultipleMessage = false;
    var odsList = vm.odsCodes.replace(/\n/g, ',').split(',');

    if (vm.regionUUID != '') {
      vm.organisationService.searchOrganisationsInParentRegionWithOdsList(vm.regionUUID, odsList)
        .subscribe(
          (result) => {
              vm.multipleSearchResults = result,
              vm.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => vm.log.error(error)
        );
    } else if (vm.dsaUUID != '' && vm.searchType == 'publisher') {
      vm.organisationService.searchPublishersFromDSAWithOdsList(vm.dsaUUID, odsList)
        .subscribe(
          (result) => {
              vm.multipleSearchResults = result,
              vm.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => vm.log.error(error)
        );
    } else if (vm.dsaUUID != '' && vm.searchType == 'subscriber') {
      vm.organisationService.searchSubscribersFromDSAWithOdsList(vm.dsaUUID, odsList)
        .subscribe(
          (result) => {
              vm.multipleSearchResults = result,
              vm.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => vm.log.error(error)
        );
    } else {
      vm.organisationService.getMultipleOrganisationsFromODSList(odsList)
        .subscribe(
          (result) => {
              vm.multipleSearchResults = result,
              vm.multipleSearchMissing = odsList.filter((x) => !result.filter(y => y.odsCode === x).length);
          },
          (error) => vm.log.error(error)
        );
    }
  }

  private addToSelection(match: Organisation) {
    if (!this.resultData.some(x => x.uuid === match.uuid)) {
      this.resultData.push(match);
    }
  }

  private removeFromSelection(match: Organisation) {
    const index = this.resultData.indexOf(match, 0);
    if (index > -1) {
      this.resultData.splice(index, 1);
    }
  }

  addMultiple() {
    const vm = this;
    vm.showMultipleMessage = false;
    vm.multipleAddedCount = 0;
    for (let match of this.multipleSearchResults) {
      if (!this.resultData.some(x => x.uuid === match.uuid)) {
        this.resultData.push(match);
        vm.multipleAddedCount++;
      }
    }
    vm.showMultipleMessage = true;
  }

  ok() {
    this.moveSelectedOrgsIntoResultData();
    this.activeModal.close(this.resultData);
  }

  cancel() {
    this.activeModal.dismiss('cancel');
  }

  checkAllOrganisations(ev) {
    console.log('selecting all');
    this.availableOrgs.forEach(x => x.selected = ev.target.checked);
  }

  getRegionOrganisations(regionUUID: string) {
    const vm = this;
    vm.regionService.getRegionOrganisations(regionUUID)
      .subscribe(
        result => {
            vm.availableOrgs = result;
            vm.availableOrgs.forEach(x => x.selected = false);
          },
        error => vm.log.error('The region organisations could not be loaded. Please try again.', error, 'Load regions organisations')
      );
  }

  getDSAPublishers(dsaUUID: string) {
    const vm = this;
    vm.dsaService.getPublishers(dsaUUID)
      .subscribe(
        result => {
          vm.availableOrgs = result;
          vm.availableOrgs.forEach(x => x.selected = false);
        },
        error => vm.log.error('The dsa publishers could not be loaded. Please try again.', error, 'Load publishers')
      );
  }

  getDSASubscribers(dsaUUID: string) {
    const vm = this;
    vm.dsaService.getSubscribers(dsaUUID)
      .subscribe(
        result => {
          vm.availableOrgs = result;
          vm.availableOrgs.forEach(x => x.selected = false);
        },
        error => vm.log.error('The dsa subscribers could not be loaded. Please try again.', error, 'Load subscribers')
      );
  }

  onTabChange(ev) {
    console.log(this.availableOrgs);
    if (ev.nextId == 'select') {
      for (let match of this.resultData) {
        let foundOrg = this.availableOrgs.findIndex(o => o.uuid === match.uuid);
        if (foundOrg > -1) {
          this.availableOrgs[foundOrg].selected = true;
        }
      }
    } else {
      this.moveSelectedOrgsIntoResultData();
    }
  }

  moveSelectedOrgsIntoResultData() {
    for (let match of this.availableOrgs) {
      if (match.selected) {
        if (!this.resultData.some(x => x.uuid === match.uuid)) {
          this.resultData.push(match);
        }
      }
    }
  }

}
