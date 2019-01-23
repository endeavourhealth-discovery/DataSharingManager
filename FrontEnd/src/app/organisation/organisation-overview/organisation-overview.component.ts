import {Component, Input, OnInit, ViewContainerRef} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {Organisation} from '../models/Organisation';
import {OrganisationService} from '../organisation.service';
import {OrganisationManagerStatistics} from '../models/OrganisationManagerStatistics';
import {FileUpload} from '../models/FileUpload';
import {LoggerService, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import { ToastsManager } from 'ng2-toastr/ng2-toastr';
import {User} from 'eds-angular4/dist/security/models/User';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-organisation-overview',
  templateUrl: './organisation-overview.component.html',
  styleUrls: ['./organisation-overview.component.css']
})
export class OrganisationOverviewComponent implements OnInit {
  organisations: Organisation[];
  file: File;
  existingOrg: Organisation;
  newOrg: Organisation;
  filesToUpload: FileUpload[] = [];
  fileList: FileList;
  currentUser: User;
  allowEdit = false;
  allowBulk = false;

  public activeProject: UserProject;

  conflictedOrgs: Organisation[];
  orgStats: OrganisationManagerStatistics[];
  orgLoadingComplete = false;
  serviceStats: OrganisationManagerStatistics[];
  serviceLoadingComplete = false;
  regionStats: OrganisationManagerStatistics[];
  regionLoadingComplete = false;

  constructor(private $modal: NgbModal,
              private organisationService: OrganisationService,
              private log: LoggerService,
              private securityService: SecurityService,
              private router: Router,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

    this.getOverview();
    this.currentUser = this.securityService.getCurrentUser();
  }

  roleChanged() {
    const vm = this;

    vm.allowEdit = false;
    vm.allowBulk = false;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    }

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Bulk') != null) {
      vm.allowBulk = true;
    }
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
    vm.orgLoadingComplete = false;
    vm.organisationService.getStatistics('organisation')
      .subscribe(result => {
          vm.orgStats = result;
          vm.orgLoadingComplete = true;
        },
        error => {
          console.log('Failed to load organisation statistics', error, 'Load service statistics');
          vm.orgLoadingComplete = true;
        }
      );
  }

  getServiceStatistics() {
    const vm = this;
    vm.serviceLoadingComplete = false;
    vm.organisationService.getStatistics('service')
      .subscribe(result => {
          vm.serviceStats = result;
          vm.serviceLoadingComplete = true;
        },
        error => {
          console.log('Failed to load service statistics', error, 'Load service statistics');
          vm.serviceLoadingComplete = true;
        }
      );
  }

  getRegionStatistics() {
    const vm = this;
    vm.regionLoadingComplete = false;
    vm.organisationService.getStatistics('region')
      .subscribe(result => {
          vm.regionStats = result;
          vm.regionLoadingComplete = true;
        },
        error => {
          console.log('Failed to load region statistics', error, 'Load region statistics');
          vm.regionLoadingComplete = true;
        }
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
      vm.log.success('Uploading File ' + fileToUpload.name, null, 'Upload file');
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
    }

    if (allUploaded) {
      vm.log.success('All files uploaded successfully.', null, 'Upload files');
      vm.log.success('Saving organisation mappings.', null, 'Upload files');
      vm.saveBulkMappings();
    }
  }

  private endUpload() {
    const vm = this;
    vm.organisationService.endUpload()
      .subscribe(
        result => {
          vm.log.success('Organisation mappings saved successfully.' , null, 'Save organisation mappings');
          vm.log.success('All organisations uploaded successfully.' , null, 'Save organisations');
          vm.getOrganisationStatistics();
          vm.getServiceStatistics();
          vm.getRegionStatistics();
          vm.getConflictingOrganisations();
        },
        error => vm.log.error('The organisation mappings could not be saved. Please try again.', error, 'Save organisation mappings')
      )
  }

  private saveBulkMappings() {
    const vm = this;
    vm.organisationService.saveMappings(10000)
      .subscribe(
        (result) => {
          if (result > 0) {
            vm.log.success(result + ' Mappings to process remaining', null, 'Process mappings');
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
          vm.log.success(result + ' Organisations uploaded successfully ' + fileToUpload.name, null, 'Upload organisations');
          vm.getNextFileToUpload();
        },
        error => vm.log.error('The organisations could not be uploaded. Please try again. ' + fileToUpload.name, error, 'Upload bulk organisations')
      );
  };

  private getConflictingOrganisations() {
    const vm = this;
    vm.organisationService.getConflictedOrganisations()
      .subscribe(result => vm.conflictedOrgs = result,
        error => vm.log.error('The conflicted organisations could not be loaded. Please try again.', error, 'Get conflicting organisations'))
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
        error => vm.log.error('The upload could not be started. Please try again.', error, 'Upload file')
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
        result => {vm.newOrg.addresses = result},
        error => vm.log.error('The organisation address could not be loaded. Please try again.', error, 'Load address')
      );

    vm.organisationService.getOrganisation(organisation.bulkConflictedWith)
      .subscribe(result => {
          vm.existingOrg = result
          vm.organisationService.getOrganisationAddresses(organisation.bulkConflictedWith)
            .subscribe(
              (result) => vm.existingOrg.addresses = result,
              (error) => vm.log.error('The organisation address could not be loaded. Please try again.', error, 'Load address')
            );
        },
        error => vm.log.error('The organisation could not be loaded. Please try again.', error, 'Load organisation')

      );
  }

  saveConflict() {
    const vm = this;
    vm.organisationService.saveOrganisation(vm.existingOrg)
      .subscribe(saved => {
          vm.removeConflict(vm.newOrg);
        },
        error => vm.log.error('The organisation could not be saved. Please try again.', error, 'Save organisation')
      );
  }

  cancelConflictResolution() {
    this.existingOrg = null;
  }

  removeConflict(org) {
    const vm = this;
    vm.organisationService.deleteOrganisation(org.uuid)
      .subscribe(
        result => vm.log.success('Conflict resolved', vm.existingOrg, 'Resolve conflict'),
        error => vm.log.error('The conflict could not be resolved. Please try again.', error, 'Resolve conflict')
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
