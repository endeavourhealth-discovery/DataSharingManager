import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {Organisation} from "../../organisation/models/Organisation";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Project} from "../models/Project";
import {DataSet} from "../../data-set/models/Dataset";
import {Cohort} from "../../cohort/models/Cohort";
import {ToastsManager} from "ng2-toastr";
import {LoggerService, SecurityService} from "eds-angular4";
import {ActivatedRoute, Router} from "@angular/router";
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ProjectService} from "../project.service";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {OrganisationPickerComponent} from "../../organisation/organisation-picker/organisation-picker.component";
import {CohortPickerComponent} from "../../cohort/cohort-picker/cohort-picker.component";
import {DataSetPickerComponent} from "../../data-set/data-set-picker/data-set-picker.component";

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
              public toastr: ToastsManager, vcr: ViewContainerRef) {
    this.toastr.setRootViewContainerRef(vcr);
  }

  ngOnInit() {
    this.checkEditPermission();
    this.paramSubscriber = this.route.params.subscribe(
      params => {
        this.performAction(params['mode'], params['id']);
      });
  }

  checkEditPermission() {
    const vm = this;
    if (vm.securityService.hasPermission('eds-dsa-manager', 'eds-dsa-manager:admin'))
      vm.allowEdit = true;
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
    DataSharingAgreementPickerComponent.open(vm.$modal, vm.dsas)
      .result.then(function
      (result: Dsa[]) { vm.dsas = result; },
      () => vm.log.info('Edit data sharing agreements cancelled')
    );
  }

  private editPublishers() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.publishers, "organisations")
      .result.then(function
      (result: Organisation[]) { vm.publishers = result; },
      () => vm.log.info('Edit publishers cancelled')
    );
  }

  private editSubscribers() {
    const vm = this;
    OrganisationPickerComponent.open(vm.$modal, vm.subscribers, "organisations")
      .result.then(function
      (result: Organisation[]) { vm.subscribers = result; },
      () => vm.log.info('Edit subscribers cancelled')
    );
  }

  private editBasePopulations() {
    const vm = this;
    console.log(vm.basePopulation);
    CohortPickerComponent.open(vm.$modal, vm.basePopulation)
      .result.then(function
      (result: Cohort[]) { vm.basePopulation= result; },
      () => vm.log.info('Edit base population cancelled')
    );
  }

  private editDataSets() {
    const vm = this;
    console.log(vm.dataSet);
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

}
