import {Component, Input, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {Region} from '../../region/models/Region';
import {OrganisationManagerStatistics} from '../models/OrganisationManagerStatistics';
import {FileUpload} from '../models/FileUpload';
import {LoggerService, SecurityService} from 'eds-angular4';
import { ToastsManager } from 'ng2-toastr/ng2-toastr';
import {User} from 'eds-angular4/dist/security/models/User';

@Component({
  selector: 'app-organisation-overview',
  templateUrl: './organisation-overview.component.html',
  styleUrls: ['./organisation-overview.component.css']
})
export class OrganisationOverviewComponent implements OnInit {
  organisations: Organisation[];
  regions: Region[] = [];
  services: Organisation[];
  file: File;
  existingOrg: Organisation;
  newOrg: Organisation;
  filesToUpload: FileUpload[] = [];
  fileList: FileList;
  currentUser: User;
  allowEdit = false;
  allowBulk = false;

  conflictedOrgs: Organisation[];
  orgStats: OrganisationManagerStatistics[];
  serviceStats: OrganisationManagerStatistics[];
  regionStats: OrganisationManagerStatistics[];

  constructor(private $modal: NgbModal,
              private organisationService: OrganisationService,
              private log: LoggerService,
              private securityService: SecurityService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.getOverview();
    this.currentUser = this.securityService.getCurrentUser();
    this.checkEditPermission();
    this.checkBulkPermission();
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
  }

  checkBulkPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:bulk'))
      vm.allowBulk = true;
  }

  getOverview() {
    const vm = this;
    vm.getOrganisationStatistics();
    vm.getServiceStatistics();
    vm.getRegionStatistics();
    vm.getConflictingOrganisations();

  }

  getOrganisationStatistics() {
    const vm = this;
    vm.organisationService.getStatistics('organisation')
      .subscribe(result => {
          vm.orgStats = result;
        },
        error => console.log('Failed to load organisation statistics', error, 'Load service statistics')
      );
  }

  getServiceStatistics() {
    const vm = this;
    vm.organisationService.getStatistics('service')
      .subscribe(result => {
          vm.serviceStats = result
        },
        error => console.log('Failed to load service statistics', error, 'Load service statistics')
      );
  }

  getRegionStatistics() {
    const vm = this;
    vm.organisationService.getStatistics('region')
      .subscribe(result => {
          vm.regionStats = result
        },
        error => console.log('Failed to load region statistics', error, 'Load region statistics')
      );
  }

  fileChange(event) {
    const vm = this;
    vm.filesToUpload = [];

    vm.fileList = event.target.files;

    if (vm.fileList.length > 0) {
      this.file = vm.fileList[0];
      for (let i = 0; i <= vm.fileList.length - 1; i++) {
        this.filesToUpload.push(<FileUpload>{
            name: vm.fileList[i].name,
            file: vm.fileList[i]
          }
        );
      }
    } else {
      this.file = null;
    }
  }

  private uploadFile(fileToUpload: FileUpload) {
    const vm = this;

    const myReader: FileReader = new FileReader();

    myReader.onloadend = function(e) {
      fileToUpload.fileData = myReader.result;
      fileToUpload.file = null;
      vm.log.success('Uploading File ' + fileToUpload.name, null, 'Upload');
      vm.sendToServer(fileToUpload);
    }

    myReader.readAsText(fileToUpload.file);
  }

  private getNextFileToUpload() {
    const vm = this;
    let allUploaded = true;
    for (const file of vm.filesToUpload) {
      if (file.success == null) {
        vm.uploadFile(file);
        vm.getOrganisationStatistics();
        allUploaded = false;
        break;
      }
    };

    if (allUploaded) {
      vm.log.success('All Uploaded Successfully', null, 'Upload');
      vm.log.success('Saving mappings now', null, 'Upload');
      vm.saveBulkMappings();
    }
  }

  private endUpload() {
    const vm = this;
    vm.organisationService.endUpload()
      .subscribe(
        result => {
          vm.log.success('Mappings saved Successfully ' , null, 'Success');
          vm.log.success('All Organisations Uploaded Successfully ' , null, 'Success');
          vm.getOrganisationStatistics();
          vm.getServiceStatistics();
          vm.getRegionStatistics();
          vm.getConflictingOrganisations();
        },
        error => vm.log.error('Failed to save mappings', error, 'Upload Bulk Organisations')
      )
  }

  private saveBulkMappings() {
    const vm = this;
    vm.organisationService.saveMappings(10000)
      .subscribe(
        (result) => {
          if (result > 0) {
            vm.log.success(result + ' Mappings to Upload', null, 'Success');
            vm.saveBulkMappings();
          } else {
            vm.endUpload();
          }
        }
      )
  }

  private sendToServer(fileToUpload: FileUpload) {
    const vm = this;
    vm.organisationService.uploadCsv(fileToUpload)
      .subscribe(result => {
          fileToUpload.success = 1;
          vm.log.success(result + ' Organisations Uploaded Successfully ' + fileToUpload.name, null, 'Success');
          vm.getNextFileToUpload();
        },
        error => vm.log.error('Failed to upload bulk organisations ' + fileToUpload.name, error, 'Upload Bulk Organisations')
      );
  };

  private getConflictingOrganisations() {
    const vm = this;
    vm.organisationService.getConflictedOrganisations()
      .subscribe(result => vm.conflictedOrgs = result,
        error => console.log('Failed to get conflicted Organisations', error, 'Get Conflicting Organisations'))
  }

  ok() {
    this.uploadFiles();
  }

  private uploadFiles() {
    const vm = this;
    vm.organisationService.startUpload()
      .subscribe(
        result => {
          vm.getNextFileToUpload();
        },
        error => vm.log.error('Error starting upload', error, 'Error')
      );

  }

  cancel() {
    this.file = null;
  }

  resolveDifferences(organisation: Organisation) {
    const vm = this;
    vm.newOrg = organisation;
    vm.organisationService.getOrganisationAddresses(organisation.uuid)
      .subscribe(
        result => {vm.newOrg.addresses = result,
          console.log(result)},
        error => vm.log.error('Error getting address', error, 'Error')
      );

    vm.organisationService.getOrganisation(organisation.bulkConflictedWith)
      .subscribe(result => {
          vm.existingOrg = result
          vm.organisationService.getOrganisationAddresses(organisation.bulkConflictedWith)
            .subscribe(
              (result) => vm.existingOrg.addresses = result,
              (error) => vm.log.error('Error getting address', error, 'Error')
            );
        },
        error => vm.log.error('Error getting address', error, 'Error')

      );
  }

  saveConflict() {
    const vm = this;
    vm.organisationService.saveOrganisation(vm.existingOrg)
      .subscribe(saved => {
          vm.removeConflict(vm.newOrg);
        },
        error => vm.log.error('Error saving', error, 'Error')
      );
  }

  cancelConflictResolution() {
    this.existingOrg = null;
  }

  removeConflict(org) {
    const vm = this;
    vm.organisationService.deleteOrganisation(org.uuid)
      .subscribe(
        result => vm.log.success('Conflict Resolved', vm.existingOrg, 'Saved'),
        error => vm.log.error('Error deleting conflict', error, 'Error')
      )

    const index = vm.conflictedOrgs.indexOf(org, 0);
    if (index > -1) {
      this.conflictedOrgs.splice(index, 1);
    }
    this.newOrg = null;
    this.existingOrg = null;

  }

  goToOrganisations() {
    this.router.navigate(['/organisations', {mode: 'organisations'}]);
  }

  goToServices() {
    this.router.navigate(['organisations', {mode: 'services'}]);
  }

  goToRegions() {
    this.router.navigate(['/regions']);
  }
}
