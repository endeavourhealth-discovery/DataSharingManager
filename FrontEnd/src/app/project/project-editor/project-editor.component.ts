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

@Component({
  selector: 'app-project-editor',
  templateUrl: './project-editor.component.html',
  styleUrls: ['./project-editor.component.css']
})
export class ProjectEditorComponent implements OnInit {
  private paramSubscriber: any;

  project: Project = <Project>{};
  dsas: Dsa[];
  publishers: Organisation[];
  subscribers: Organisation[];
  basePopulation: Cohort[];
  dataSet: DataSet[];
  allowEdit = false;
  userList: User[];
  authToShare: AuthorityToShare[];
  disableStatus = false;

  public activeProject: UserProject;

  projectApplicationPolicy: ProjectApplicationPolicy;
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
    {num: 5, name: 'One off'}
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
              private userManagerNotificationService: UserManagerNotificationService) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode'], params['id']);
      });

    this.userManagerNotificationService.activeUserProject.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });

    this.getAvailableApplicationPolicies();
    this.getUserList();
  }

  roleChanged() {
    const vm = this;

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
    }
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
          vm.project = result;
          vm.getLinkedDsas();
          vm.getLinkedBasePopulations();
          vm.getLinkedPublishers();
          vm.getLinkedSubscribers();
          vm.getLinkedDataSets();
          vm.getProjectApplicationPolicy();
          vm.getUsersAssignedToProject();
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
    vm.project.basePopulation = {};
    for (let idx in this.basePopulation) {
      const coh: Cohort = this.basePopulation[idx];
      this.project.basePopulation[coh.uuid] = coh.name;
    }

    // Populate subscribers before save
    vm.project.dataSet = {};
    for (let idx in this.dataSet) {
      const ds: DataSet = this.dataSet[idx];
      this.project.dataSet[ds.uuid] = ds.name;
    }

    vm.projectService.saveProject(vm.project)
      .subscribe(saved => {
          vm.project.uuid = saved;
          vm.log.success('Project saved', vm.project, 'Save project');
          vm.saveApplicationPolicy();
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
    CohortPickerComponent.open(vm.$modal, vm.basePopulation)
      .result.then(function
      (result: Cohort[]) { vm.basePopulation= result; },
      () => vm.log.info('Edit base population cancelled')
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
        result => vm.basePopulation = result,
        error => vm.log.error('The associated base population could not be loaded. Please try again.', error, 'Load associated base population')
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

}
