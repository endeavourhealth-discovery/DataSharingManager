import {Component, OnInit} from '@angular/core';
import {DataSet} from "../models/Dataset";
import {LoggerService, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DataSetService} from "../data-set.service";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Dpa} from "../../data-processing-agreement/models/Dpa";

@Component({
  selector: 'app-data-set-editor',
  templateUrl: './data-set-editor.component.html',
  styleUrls: ['./data-set-editor.component.scss']
})
export class DataSetEditorComponent implements OnInit {

  dataset: DataSet;
  processingAgreements: Dpa[] = [];
  processingAgreementsDetailsToShow = new Dpa().getDisplayItems();
  public activeProject: UserProject;
  private paramSubscriber: any;
  allowEdit = false;
  superUser = false;
  userId: string;

  constructor(private log: LoggerService,
              private dataSetService: DataSetService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerService: UserManagerService) {
  }

  ngOnInit() {
    this.userManagerService.onProjectChange.subscribe(active => {
      this.activeProject = active;
      this.roleChanged();
    });
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Super User') != null) {
      this.allowEdit = true;
      this.superUser = true;
      this.userId = null;
    } else if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
      this.superUser = false;
      this.userId = this.activeProject.userId;
    } else {
      this.allowEdit = false;
      this.superUser = false;
      this.userId = this.activeProject.userId;
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
    this.dataSetService.getDataSet(uuid)
      .subscribe(result =>  {
          this.dataset = result;
          this.getProcessingAgreements();
        },
        error => this.log.error('The data set could not be loaded. Please try again.')
      );
  }

  private getProcessingAgreements() {
    this.dataSetService.getLinkedDpas(this.dataset.uuid)
      .subscribe(
        result => {
          this.processingAgreements = result;
        },
        error => this.log.error('The associated data processing agreements could not be loaded. Please try again.')
      );
  }

  processingAgreementClicked(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  deleteDPAs() {
    //TODO
  }

  addAddress() {
    //TODO
  }

  save(close: boolean) {
    this.dataset.dpas = {};
    for (const idx in this.processingAgreements) {
      const dpa: Dpa = this.processingAgreements[idx];
      this.dataset.dpas[dpa.uuid] = dpa.name;
    }

    this.dataSetService.saveDataSet(this.dataset)
      .subscribe(saved => {
          this.dataset.uuid = saved;
          this.log.success('Data Set saved successfully.');
          if (close) { window.history.back(); }
        },
        error => this.log.error('The Data Set could not be saved. Please try again.')
      );
  }
}
