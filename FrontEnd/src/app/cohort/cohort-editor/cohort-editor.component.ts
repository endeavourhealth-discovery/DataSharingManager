import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {CohortService} from '../cohort.service';
import {LoggerService, SecurityService, UserManagerNotificationService} from 'eds-angular4';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Dpa} from '../../data-processing-agreement/models/Dpa';
import {DataProcessingAgreementPickerComponent} from '../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component';
import {Cohort} from "../models/Cohort";
import {ToastsManager} from "ng2-toastr";
import {UserProject} from "eds-angular4/dist/user-manager/models/UserProject";

@Component({
  selector: 'app-cohort-editor',
  templateUrl: './cohort-editor.component.html',
  styleUrls: ['./cohort-editor.component.css']
})
export class CohortEditorComponent implements OnInit {
  private paramSubscriber: any;
  cohort: Cohort = <Cohort>{};
  dpas: Dpa[] = [];
  allowEdit = false;

  public activeProject: UserProject;

  dpaDetailsToShow = new Dpa().getDisplayItems();

  consents = [
    {num: 0, name : 'Explicit Consent'},
    {num: 1, name : 'Implied Consent'}
  ];

  constructor(private $modal: NgbModal,
              private log: LoggerService,
              private cohortService: CohortService,
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
    this.cohort = {
      name : ''
    } as Cohort;
  }

  load(uuid: string) {
    const vm = this;
    vm.cohortService.getCohort(uuid)
      .subscribe(result =>  {
          vm.cohort = result;
          vm.getLinkedDpas();
        },
        error => vm.log.error('The cohort could not be loaded. Please try again.', error, 'Load cohort')
      );
  }

  save(close: boolean) {
    const vm = this;

    // Populate Data Processing Agreements before save
    vm.cohort.dpas = {};
    for (const idx in this.dpas) {
      let dpa: Dpa = this.dpas[idx];
      this.cohort.dpas[dpa.uuid] = dpa.name;
    }

    vm.cohortService.saveCohort(vm.cohort)
      .subscribe(saved => {
          vm.cohort.uuid = saved;
          vm.log.success('Cohort saved successfully', vm.cohort, 'Save cohort');
          if (close) { vm.close(); }
        },
        error => vm.log.error('The cohort could not be saved. Please try again.', error, 'Save cohort')
      );
  }

  close() {
    window.history.back();
  }

  private editDataProcessingAgreements() {
    const vm = this;
    DataProcessingAgreementPickerComponent.open(vm.$modal, vm.dpas)
      .result.then(function
      (result: Dpa[]) { vm.dpas = result; },
      () => vm.log.info('Edit data processing agreements cancelled')
    );
  }

  private editDataProcessingAgreement(item: Dpa) {
    const vm = this;
    DataProcessingAgreementPickerComponent.open(vm.$modal, vm.dpas)
      .result.then(function
      (result: Dpa[]) { vm.dpas = result; },
      () => vm.log.info('Edit data processing agreements cancelled')
    );
  }

  private getLinkedDpas() {
    const vm = this;
    vm.cohortService.getLinkedDpas(vm.cohort.uuid)
      .subscribe(
        result => vm.dpas = result,
        error => vm.log.error('The associated data processing agreements could not be loaded. Please try again.', error, 'Load associated data processing agreements')
      );
  }

}
