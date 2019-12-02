import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {Organisation} from "../../organisation/models/Organisation";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Project} from "../models/Project";
import {DataSet} from "../../data-set/models/Dataset";
import {Cohort} from "../../cohort/models/Cohort";
import {ToastsManager} from "ng2-toastr";
import {
  LoggerService,
  MessageBoxDialog,
  SecurityService,
  UserManagerNotificationService,
  UserManagerService
} from "eds-angular4";
import {ActivatedRoute, Router} from "@angular/router";
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ProjectService} from "../project.service";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {OrganisationPickerComponent} from "../../organisation/organisation-picker/organisation-picker.component";
import {CohortPickerComponent} from "../../cohort/cohort-picker/cohort-picker.component";
import {DataSetPickerComponent} from "../../data-set/data-set-picker/data-set-picker.component";
import {ProjectApplicationPolicy} from "../models/ProjectApplicationPolicy";
import {ApplicationPolicy} from "../models/ApplicationPolicy";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";
import {User} from "eds-angular4/dist/security/models/User";
import {AuthorityToShare} from "../models/AuthorityToShare";
import { DatePipe } from '@angular/common';
import {Documentation} from "../../documentation/models/Documentation";
import {DocumentationService} from "../../documentation/documentation.service";
import {ExtractTechnicalDetails} from "../models/ExtractTechnicalDetails";
import {Observable} from "rxjs";
import {Http, URLSearchParams} from "@angular/http";
import {Schedule} from "../../scheduler/models/Schedule";
import {SchedulerPickerComponent} from "../../scheduler/scheduler-picker/scheduler-picker.component";

@Component({
  selector: 'app-project-editor',
  templateUrl: './project-editor.component.html',
  styleUrls: ['./project-editor.component.css']
})
export class ProjectEditorComponent implements OnInit {
  private paramSubscriber: any;

  mode: string;
  projectId: string;
  project: Project = <Project>{};
  dsas: Dsa[] = [];
  publishers: Organisation[] = [];
  subscribers: Organisation[] = [];
  cohorts: Cohort[] = [];
  dataSet: DataSet[] = [];
  documentations: Documentation[] = [];
  extractTechnicalDetails: ExtractTechnicalDetails = <ExtractTechnicalDetails>{};
  allowEdit = false;
  superUser = false;
  userList: User[] = [];
  authToShare: AuthorityToShare[] = [];
  disableStatus = false;

  file: File;
  pdfSrc: any;

  // Extract technical details
  sftpHostPublicKeyFile: File;
  sftpHostPublicKeyFileSrc: any;
  sftpClientPrivateKeyFile: File;
  sftpClientPrivateKeySrc: any;
  pgpCustomerPublicKeyFile: File;
  pgpCustomerPublicKeyFileSrc: any;
  pgpInternalPublicKeyFile: File;
  pgpInternalPublicKeyFileSrc: any;

  public activeProject: UserProject;

  projectApplicationPolicy: ProjectApplicationPolicy;
  schedules: Schedule[] = [];
  availablePolicies: ApplicationPolicy[];
  selectedApplicationPolicy: ApplicationPolicy;

  storageProtocols = [
    {num: 0, name: 'Audit only'},
    {num: 1, name: 'Temporary Store And Forward'},
    {num: 2, name: 'Permanent Record Store'}
  ];

  consents = [
    {num: 0, name : 'Explicit Consent'},
    {num: 1, name : 'Implied Consent'}
  ];

  deidentificationLevel = [
    {num: 0, name: 'Patient identifiable data'},
    {num: 1, name: 'Pseudonymised data'}
  ];

  projectTypes = [
    {num: 0, name: 'Extract'},
    {num: 1, name: 'Query'}
  ];

  securityArchitectures = [
    {num: 0, name: 'TLS/MA'},
    {num: 1, name: 'Secure FTP'}
  ];

  securityInfrastructures = [
    {num: 0, name: 'N3'},
    {num: 1, name: 'PSN'},
    {num: 1, name: 'Internet'}
  ];

  businessCaseStatuses = [
    {num: 0, name: 'Submitted'},
    {num: 1, name: 'Approved'}
  ];

  flowScheduleIds = [
    {num: 0, name: 'Daily'},
    {num: 1, name: 'On Demand'},
    {num: 2, name: 'Weekly'},
    {num: 3, name: 'Monthly'},
    {num: 4, name: 'Annually'},
    {num: 5, name: 'One off'},
    {num: 6, name: 'Quarterly'}
  ];

  outputFormat = [
    {num: 0, name: 'FHIR'},
    {num: 1, name: 'CSV'}
  ];

  status = [
    {num: 0, name : 'Active'},
    {num: 1, name : 'Inactive'}
  ];

  dsaDetailsToShow = new Dsa().getDisplayItems();
  dataSetDetailsToShow = new DataSet().getDisplayItems();
  cohortDetailsToShow = new Cohort().getDisplayItems();
  OrganisationDetailsToShow = new Organisation().getDisplayItems();


  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private projectService: ProjectService,
              private securityService: SecurityService,
              private router: Router,
              private route: ActivatedRoute,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService,
              private documentationService: DocumentationService,
              private http: Http,
              private datePipe: DatePipe) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {

    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      vm.allowEdit = true;
      vm.superUser = true;
    } else if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
      vm.superUser = false;
    } else {
      vm.allowEdit = false;
      vm.superUser = false;
    }

    if (!vm.superUser) {
      if (vm.activeProject.projectId != vm.projectId) {
        vm.allowEdit = false;
      }
    }

    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.mode = params['mode'];
        this.projectId =  params['id'];
        this.performAction(params['mode'], params['id']);
      });

    this.getAvailableApplicationPolicies();
    this.getUserList();
    this.getSchedule();
  }

  getSchedule() {
    const vm = this;
  }

  getAvailableApplicationPolicies() {
    const vm = this;
    vm.projectService.getAvailableProjectApplicationPolicy()
      .subscribe(
        (result) => {
          vm.availablePolicies = result;
        },
        (error) => {
          vm.log.error('Available application policies could not be loaded. Please try again.', error, 'Load available application policies');
        }
      );
  }

  getUserList() {
    const vm = this;
    vm.projectService.getUsers()
      .subscribe(
        (result) => {
          vm.userList = result;
        },
        (error) => {
          vm.log.error('User list could not be loaded. Please try again.', error, 'Load user list');
        }
      );
  }

  getUsersAssignedToProject() {
    const vm = this;
    vm.projectService.getUsersAssignedToProject(vm.project.uuid)
      .subscribe(
        (result) => {
          vm.authToShare = result;
        },
        (error) => {
          vm.log.error('Authority to share could not be loaded. Please try again.', error, 'Load authority to share');
        }
      );
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
    this.project = {
      name : ''
    } as Project;
  }

  load(uuid: string) {
    const vm = this;
    vm.projectService.getProject(uuid)
      .subscribe(result =>  {
          vm.project = result
          vm.project.startDate = this.datePipe.transform(vm.project.startDate,"yyyy-MM-dd");
          vm.project.endDate = this.datePipe.transform(vm.project.endDate,"yyyy-MM-dd");
          vm.getLinkedDsas();
          vm.getLinkedBasePopulations();
          vm.getLinkedPublishers();
          vm.getLinkedSubscribers();
          vm.getLinkedDataSets();
          vm.getProjectApplicationPolicy();
          vm.getUsersAssignedToProject();
          vm.getAssociatedDocumentation();
          vm.getAssociatedExtractTechnicalDetails();
          vm.getSchedule();
        },
        error => vm.log.error('The project could not be loaded. Please try again.', error, 'Load project')
      );
  }

  save(close: boolean) {
    const vm = this;
    // Populate Data Sharing Agreements before save
    vm.project.dsas = {};
    for (let idx in this.dsas) {
      const dsa: Dsa = this.dsas[idx];
      this.project.dsas[dsa.uuid] = dsa.name;
    }

    // Populate publishers before save
    vm.project.publishers = {};
    for (let idx in this.publishers) {
      const pub: Organisation = this.publishers[idx];
      this.project.publishers[pub.uuid] = pub.name;
    }

    // Populate subscribers before save
    vm.project.subscribers = {};
    for (let idx in this.subscribers) {
      const sub: Organisation = this.subscribers[idx];
      this.project.subscribers[sub.uuid] = sub.name;
    }

    // Populate subscribers before save
    vm.project.cohorts = {};
    for (let idx in this.cohorts) {
      const coh: Cohort = this.cohorts[idx];
      this.project.cohorts[coh.uuid] = coh.name;
    }

    // Populate subscribers before save
    vm.project.dataSets = {};
    for (let idx in this.dataSet) {
      const ds: DataSet = this.dataSet[idx];
      this.project.dataSets[ds.uuid] = ds.name;
    }

    // Populate documents before save
    vm.project.documentations = [];
    vm.project.documentations = vm.documentations;

    // Populate extract technical details before save
    vm.project.extractTechnicalDetails = null;
    vm.project.extractTechnicalDetails = vm.extractTechnicalDetails;

    console.log(vm.project);
    vm.projectService.saveProject(vm.project)
      .subscribe(saved => {
          vm.project.uuid = saved;
          vm.log.success('Project saved', vm.project, 'Save project');
          vm.saveApplicationPolicy();
          vm.getAssociatedExtractTechnicalDetails();
          vm.getSchedule();
          if (close) { vm.close(); }
        },
        error => vm.log.error('The project could not be saved. Please try again.', error, 'Save project')
      );
  }

  close() {
    window.history.back();
  }

  private editDataSharingAgreements() {
    const vm = this;
    DataSharingAgreementPickerComponent.open(vm.$modal, vm.dsas, 1)
      .result.then(function
      (result: Dsa[]) { vm.dsas = result; },
      () => vm.log.info('Edit data sharing agreements cancelled')
    );
  }

  private editPublishers() {
    const vm = this;
    if (!vm.dsas[0]) {
      MessageBoxDialog.open(vm.$modal, 'Edit publishers', 'The project must be associated with a data sharing agreement before editing publishers', 'Ok', '')
        .result.then();
    } else {
      OrganisationPickerComponent.open(vm.$modal, vm.publishers, "publisher", '', '', vm.dsas[0].uuid)
        .result.then(function
        (result: Organisation[]) {
          vm.publishers = result;
        },
        () => vm.log.info('Edit publishers cancelled')
      );
    }
  }

  private editSubscribers() {
    const vm = this;
    if (!vm.dsas[0]) {
      MessageBoxDialog.open(vm.$modal, 'Edit subscribers', 'The project must be associated with a data sharing agreement before editing subscribers', 'Ok', '')
        .result.then();
    } else {
      OrganisationPickerComponent.open(vm.$modal, vm.subscribers, "subscriber", '', '', vm.dsas[0].uuid)
        .result.then(function
        (result: Organisation[]) {
          vm.subscribers = result;
        },
        () => vm.log.info('Edit subscribers cancelled')
      );
    }
  }

  private editBasePopulations() {
    const vm = this;
    CohortPickerComponent.open(vm.$modal, vm.cohorts)
      .result.then(function
      (result: Cohort[]) { vm.cohorts= result; },
      () => vm.log.info('Edit cohort cancelled')
    );
  }

  private editDataSets() {
    const vm = this;
    DataSetPickerComponent.open(vm.$modal, vm.dataSet)
      .result.then(function
      (result: DataSet[]) { vm.dataSet = result; },
      () => vm.log.info('Edit data set cancelled')
    );
  }

  private getLinkedDsas() {
    const vm = this;
    vm.projectService.getLinkedDsas(vm.project.uuid)
      .subscribe(
        result => vm.dsas = result,
        error => vm.log.error('The associated data sharing agreements could not be loaded. Please try again.', error, 'Load associated data sharing agreements')
      );
  }

  private getLinkedPublishers() {
    const vm = this;
    vm.projectService.getLinkedPublishers(vm.project.uuid)
      .subscribe(
        result => vm.publishers = result,
        error => vm.log.error('The associated publishers could not be loaded. Please try again.', error, 'Load associated publishers')
      );
  }

  private getLinkedSubscribers() {
    const vm = this;
    vm.projectService.getLinkedSubscribers(vm.project.uuid)
      .subscribe(
        result => vm.subscribers = result,
        error => vm.log.error('The associated subscribers could not be loaded. Please try again.', error, 'Load associated subscribers')
      );
  }

  private getLinkedBasePopulations() {
    const vm = this;
    vm.projectService.getLinkedBasePopulation(vm.project.uuid)
      .subscribe(
        result => vm.cohorts = result,
        error => vm.log.error('The associated cohort could not be loaded. Please try again.', error, 'Load associated cohort')
      );
  }

  private getLinkedDataSets() {
    const vm = this;
    vm.projectService.getLinkedDataSets(vm.project.uuid)
      .subscribe(
        result => vm.dataSet = result,
        error => vm.log.error('The associated data sets could not be loaded. Please try again.', error, 'Load associated data sets')
      );
  }

  getProjectApplicationPolicy() {
    const vm = this;
    vm.projectService.getProjectApplicationPolicy(vm.project.uuid)
      .subscribe(
        (result) => {
          vm.projectApplicationPolicy = result;
          vm.selectedApplicationPolicy = vm.availablePolicies.find(r => {
            return r.id === vm.projectApplicationPolicy.applicationPolicyId;
          });
        },
        (error) => {
          vm.log.error('Project application policy could not be loaded. Please try again.', error, 'Load project application policy');
        }
      );
  }

  private getAssociatedDocumentation() {
    const vm = this;
    vm.documentationService.getAllAssociatedDocuments(vm.project.uuid, '14')
      .subscribe(
        result => vm.documentations = result,
        error => vm.log.error('The associated documentation could not be loaded. Please try again.', error, 'Load associated documentation')
      );
  }

  removeFromDocumentation(match: Documentation) {
    const index = this.documentations.indexOf(match, 0);
    if (index > -1) {
      this.documentations.splice(index, 1);
    }
  }

  changeUserApplicationPolicy(policyId: string) {
    const vm = this;
    let changedPolicy = new ProjectApplicationPolicy();
    changedPolicy.projectUuid = vm.project.uuid;
    changedPolicy.applicationPolicyId = policyId;
    vm.projectApplicationPolicy = changedPolicy;
  }

  saveApplicationPolicy() {
    const vm = this;
    vm.projectService.saveProjectApplicationPolicy(vm.projectApplicationPolicy)
      .subscribe(
        (response) => {

        },
        (error) => vm.log.error('Project application policy could not be saved. Please try again.', error, 'Save project application policy')
      );
  }

  checkEndDate() {
    const vm = this;

    if (vm.project.endDate === null) {
      vm.disableStatus = false;
      return;
    }

    var today = new Date();
    today.setHours(0,0,0,0);
    var endDate = new Date(vm.project.endDate)

    if (endDate < today) {
      vm.project.projectStatusId = 1;
      vm.disableStatus = true;
    } else {
      vm.disableStatus = false;
    }
  }

  fileChange(event) {
    const fileList: FileList = event.target.files;
    if (fileList.length > 0) {
      this.file = fileList[0];
    } else {
      this.file = null;
    }
  }

  private uploadFile() {
    const vm = this;
    const myReader: FileReader = new FileReader();

    myReader.onloadend = function(e){
      // you can perform an action with readed data here
      vm.log.success('Uploading complete', null, 'Upload document');
      vm.pdfSrc = myReader.result;
      const newDoc: Documentation = new Documentation();
      newDoc.fileData = myReader.result;
      newDoc.title = vm.file.name;
      newDoc.filename = vm.file.name;
      vm.documentations.push(newDoc);
    }

    myReader.readAsDataURL(vm.file);
  }

  ok() {
    this.uploadFile();
  }

  cancel() {
    this.file = null;
  }

  private getAssociatedExtractTechnicalDetails() {
    const vm = this;
    vm.getAssociatedExtractTechDetails().subscribe(
      result => vm.extractTechnicalDetails = result,
      error => vm.log.error('The associated extract technical details could not be loaded. Please try again.', error, 'Load associated extract technical details')
    );
  }

  private getAssociatedExtractTechDetails(): Observable<ExtractTechnicalDetails> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('parentUuid', vm.project.uuid);
    params.set('parentType', '14');
    return vm.http.get('api/extractTechnicalDetails/associated', { search : params })
      .map((response) => response.json());
  }

  extractTechnicalDetailsWhichFileSelect(event, whichFile) {
    switch (whichFile) {
      case 1:
        const sftpHostPublicKeyFileList: FileList = event.target.files;
        if (sftpHostPublicKeyFileList.length > 0) {
          this.sftpHostPublicKeyFile = sftpHostPublicKeyFileList[0];
        } else {
          this.sftpHostPublicKeyFile = null;
        };
        break;

      case 2:
        const sftpClientPrivateKeyFileList: FileList = event.target.files;
        if (sftpClientPrivateKeyFileList.length > 0) {
          this.sftpClientPrivateKeyFile = sftpClientPrivateKeyFileList[0];
        } else {
          this.sftpClientPrivateKeyFile = null;
        };
        break;

      case 3:
        const pgpCustomerPublicKeyFileList: FileList = event.target.files;
        if (pgpCustomerPublicKeyFileList.length > 0) {
          this.pgpCustomerPublicKeyFile = pgpCustomerPublicKeyFileList[0];
        } else {
          this.pgpCustomerPublicKeyFile = null;
        };
        break;

      case 4:
        const pgpInternalPublicKeyFileList: FileList = event.target.files;
        if (pgpInternalPublicKeyFileList.length > 0) {
          this.pgpInternalPublicKeyFile = pgpInternalPublicKeyFileList[0];
        } else {
          this.pgpInternalPublicKeyFile = null;
        };
        break;
    }
  }

  extractTechnicalDetailsWhichFileCancel(whichFile) {
    switch (whichFile) {
      case 1:
        this.sftpHostPublicKeyFile = null;
        break;

      case 2:
        this.sftpClientPrivateKeyFile = null;
        break;

      case 3:
        this.pgpCustomerPublicKeyFile = null;
        break;

      case 4:
        this.pgpInternalPublicKeyFile = null;
        break;
    }
  }

  extractTechnicalDetailsWhichFileUpload(whichFile) {
    const vm = this;
    switch (whichFile) {
      case 1:
        const mySftpHostPublicKeyFileReader: FileReader = new FileReader();

        mySftpHostPublicKeyFileReader.onloadend = function (e) {
          vm.log.success('Uploading complete', null, 'Upload SFTP host public key file');
          vm.sftpHostPublicKeyFileSrc = mySftpHostPublicKeyFileReader.result;
          vm.extractTechnicalDetails.sftpHostPublicKeyFileData = mySftpHostPublicKeyFileReader.result;
          vm.extractTechnicalDetails.sftpHostPublicKeyFilename = vm.sftpHostPublicKeyFile.name;
        };
        mySftpHostPublicKeyFileReader.readAsDataURL(vm.sftpHostPublicKeyFile);
        break;

      case 2:
        const mySftpClientPrivateKeyFileReader: FileReader = new FileReader();

        mySftpClientPrivateKeyFileReader.onloadend = function (e) {
          vm.log.success('Uploading complete', null, 'Upload SFTP client private key file');
          vm.sftpClientPrivateKeySrc = mySftpClientPrivateKeyFileReader.result;
          vm.extractTechnicalDetails.sftpClientPrivateKeyFileData = mySftpClientPrivateKeyFileReader.result;
          vm.extractTechnicalDetails.sftpClientPrivateKeyFilename = vm.sftpClientPrivateKeyFile.name;
        };
        mySftpClientPrivateKeyFileReader.readAsDataURL(vm.sftpClientPrivateKeyFile);
        break;

      case 3:
        const myPpgCustomerPublicKeyFileReader: FileReader = new FileReader();

        myPpgCustomerPublicKeyFileReader.onloadend = function (e) {
          vm.log.success('Uploading complete', null, 'Upload PGP customer public key file');
          vm.pgpCustomerPublicKeyFileSrc = myPpgCustomerPublicKeyFileReader.result;
          vm.extractTechnicalDetails.pgpCustomerPublicKeyFileData = myPpgCustomerPublicKeyFileReader.result;
          vm.extractTechnicalDetails.pgpCustomerPublicKeyFilename = vm.pgpCustomerPublicKeyFile.name;
        };
        myPpgCustomerPublicKeyFileReader.readAsDataURL(vm.pgpCustomerPublicKeyFile);
        break;

      case 4:
        const myPpgInternalPublicKeyFileReader: FileReader = new FileReader();

        myPpgInternalPublicKeyFileReader.onloadend = function (e) {
          vm.log.success('Uploading complete', null, 'Upload PGP internal public key file');
          vm.pgpInternalPublicKeyFileSrc = myPpgInternalPublicKeyFileReader.result;
          vm.extractTechnicalDetails.pgpInternalPublicKeyFileData = myPpgInternalPublicKeyFileReader.result;
          vm.extractTechnicalDetails.pgpInternalPublicKeyFilename = vm.pgpInternalPublicKeyFile.name;
        };
        myPpgInternalPublicKeyFileReader.readAsDataURL(vm.pgpInternalPublicKeyFile);
        break;
    }
  }

  private editSchedules() {
    const vm = this;
    SchedulerPickerComponent.open(vm.$modal, vm.schedules)
      .result.then(function
      (result: Schedule[]) {
        vm.schedules = result;
      },
      () => vm.log.info('Set schedule cancelled')
    );
  }

  delete(schedule: Schedule) {
    const vm = this;
    MessageBoxDialog.open(vm.$modal, 'Delete schedule', 'Are you sure that you want to delete <b>' + schedule.cronDescription + '</b>?', 'Delete schedule', 'Cancel')
      .result.then(
      () => vm.doDelete(schedule),
      () => vm.log.info('Delete cancelled')
    );
  }

  doDelete(schedule: Schedule) {
    const vm = this;
    const index = vm.schedules.indexOf(schedule);
    vm.schedules.splice(index, 1);
    vm.log.success('Schedule deleted', schedule, 'Delete schedule');
    /*
    vm.projectService.deleteProject(item.uuid)
      .subscribe(
        () => {
          const index = vm.projects.indexOf(item);
          vm.projects.splice(index, 1);
          vm.log.success('Project deleted', item, 'Delete project');
        },
        (error) => vm.log.error('The project could not be deleted. Please try again.', error, 'Delete project')
      );
    */
  }
}
