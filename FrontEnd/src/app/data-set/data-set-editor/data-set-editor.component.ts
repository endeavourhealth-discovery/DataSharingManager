import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {DataSetService} from '../data-set.service';
import {ActivatedRoute, Router} from '@angular/router';
import {LoggerService, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ToastsManager} from 'ng2-toastr';
import {DataSet} from "../models/Dataset";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-data-set-editor',
  templateUrl: './data-set-editor.component.html',
  styleUrls: ['./data-set-editor.component.css']
})
export class DataSetEditorComponent implements OnInit {
  private paramSubscriber: any;
  public accordionClass = 'accordionClass';

  dataset: DataSet = <DataSet>{};
  dpas: Dpa[] = [];
  allowEdit = false;

  public activeProject: UserProject;

  datasetDetailsToShow = new DataSet().getDisplayItems();
  dpaDetailsToShow = new Dpa().getDisplayItems();

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private dataSetService: DataSetService,
              private securityService: SecurityService,
              private router: Router,
              private route: ActivatedRoute,
              public toastr: ToastsManager, vcr: ViewContainerRef,
              private userManagerNotificationService: UserManagerNotificationService) {
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

    if (vm.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      vm.allowEdit = true;
    } else {
      vm.allowEdit = false;
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
    this.dataset = {
      name : ''
    } as DataSet;
  }

  load(uuid: string) {
    const vm = this;
    vm.dataSetService.getDataSet(uuid)
      .subscribe(result =>  {
          vm.dataset = result;
          vm.getLinkedDPAs();
        },
        error => vm.log.error('The data set could not be loaded. Please try again.', error, 'Load data set')
      );
  }

  private getLinkedDPAs() {
    const vm = this;
    vm.dataSetService.getLinkedDpas(vm.dataset.uuid)
      .subscribe(
        result => vm.dpas = result,
        error => vm.log.error('The associated data processing agreements could not be loaded. Please try again.', error, 'Load associated data processing agreements')
      );
  }

  save(close: boolean) {
    const vm = this;
    // Populate data processing agreements before save
    vm.dataset.dpas = {};
    for (const idx in this.dpas) {
      const dpa: Dpa = this.dpas[idx];
      this.dataset.dpas[dpa.uuid] = dpa.name;
    }

    vm.dataSetService.saveDataSet(vm.dataset)
      .subscribe(saved => {
          vm.dataset.uuid = saved;
          vm.log.success('Data set saved', vm.dataset, 'Save data set');

          if (close) { vm.close(); }
        },
        error => vm.log.error('The data set could not be saved. Please try again.', error, 'Save data set')
      );
  }

  private editDataProcessingAgreements() {
    const vm = this;
    DataProcessingAgreementPickerComponent.open(vm.$modal, vm.dpas)
      .result.then(function
      (result: Dpa[]) { vm.dpas = result; },
      () => vm.log.info('Edit data processing agreements cancelled')
    );
  }

  close() {
    window.history.back();
  }

}
