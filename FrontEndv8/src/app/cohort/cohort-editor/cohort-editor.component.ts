import {Component, OnInit} from '@angular/core';
import {Cohort} from "../models/Cohort";
import {CohortService} from '../cohort.service';
import {LoggerService, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from '@angular/router';
import {Dpa} from '../../data-processing-agreement/models/Dpa';
//import {DataProcessingAgreementPickerComponent} from '../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";

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

  constructor(private log: LoggerService,
              private cohortService: CohortService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
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
    this.cohortService.getCohort(uuid)
      .subscribe(result => {
          this.cohort = result;
          this.getLinkedDpas();
        },
        error => this.log.error('The cohort could not be loaded. Please try again.'/*, error, 'Load cohort'*/)
      );
  }

  save(close: boolean) {
    // Populate Data Processing Agreements before save
    this.cohort.dpas = {};
    for (const idx in this.dpas) {
      let dpa: Dpa = this.dpas[idx];
      this.cohort.dpas[dpa.uuid] = dpa.name;
    }

    this.cohortService.saveCohort(this.cohort)
      .subscribe(saved => {
          this.cohort.uuid = saved;
          this.log.success('Cohort saved successfully'/*, vm.cohort, 'Save cohort'*/);
          if (close) {this.close();}
        },
        error => this.log.error('The cohort could not be saved. Please try again.'/*, error, 'Save cohort'*/)
      );
  }

  close() {
    window.history.back();
  }

  private editDataProcessingAgreements() {
    /*DataProcessingAgreementPickerComponent.open(this.$modal, this.dpas)
      .result.then(function
      (result: Dpa[]) {this.dpas = result;},
      () => this.log.info('Edit data processing agreements cancelled')
    );*/
  }

  private editDataProcessingAgreement(item: Dpa) {
    /*DataProcessingAgreementPickerComponent.open(this.$modal, this.dpas)
      .result.then(function
      (result: Dpa[]) {this.dpas = result;},
      () => this.log.info('Edit data processing agreements cancelled')
    );*/
  }

  private getLinkedDpas() {
    this.cohortService.getLinkedDpas(this.cohort.uuid)
      .subscribe(
        result => this.dpas = result,
        error => this.log.error('The associated data processing agreements could not be loaded. Please try again.'/*, error, 'Load associated data processing agreements'*/)
      );
  }

}
