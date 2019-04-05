import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {LoggerService, SecurityService, UserManagerNotificationService} from "eds-angular4";
import {OrganisationService} from "../../organisation/organisation.service";
import {ToastsManager} from "ng2-toastr";
import {Router} from "@angular/router";
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";
import {FileUpload} from "../../organisation/models/FileUpload";
import {Organisation} from "../../organisation/models/Organisation";

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {
  allowEdit = false;
  allowBulk = false;

  public activeProject: UserProject;

  file: File;
  existingOrg: Organisation;
  newOrg: Organisation;
  filesToUpload: FileUpload[] = [];
  fileList: FileList;

  conflictedOrgs: Organisation[];

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

    this.getConfig();
  }

  roleChanged() {
    const vm = this;

    vm.allowEdit = false;
    vm.allowBulk = false;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    }

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Config') != null) {
      vm.allowBulk = true;
    }
  }

  getConfig() {
    const vm = this;
    vm.getConflictingOrganisations();
  }

  private getConflictingOrganisations() {
    const vm = this;
    vm.organisationService.getConflictedOrganisations()
      .subscribe(result => {vm.conflictedOrgs = result; },
        error => vm.log.error('The conflicted organisations could not be loaded. Please try again.', error, 'Get conflicting organisations'))
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

}
