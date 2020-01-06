import { Component, OnInit } from '@angular/core';
import {Dpa} from "../models/Dpa";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Purpose} from "../../data-sharing-agreement/models/Purpose";
import {Organisation} from "../../organisation/models/Organisation";
import {Documentation} from "../../documentation/models/Documentation";
import {LoggerService, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DataProcessingAgreementService} from "../data-processing-agreement.service";
import {DatePipe} from "@angular/common";
import {Region} from "../../region/models/Region";

@Component({
  selector: 'app-data-processing-agreement-editor',
  templateUrl: './data-processing-agreement-editor.component.html',
  styleUrls: ['./data-processing-agreement-editor.component.scss']
})
export class DataProcessingAgreementEditorComponent implements OnInit {

  dpa: Dpa;
  public activeProject: UserProject;
  private paramSubscriber: any;
  allowEdit = false;
  superUser = false;
  userId: string;

  purposes: Purpose[] = [] = [];
  benefits: Purpose[] = [] = [];
  purposesDetailsToShow = new Purpose().getDisplayItems();

  regions: Region[] = [];
  regionsDetailsToShow = new Region().getDisplayItems();
  publishers: Organisation[] = [];
  publishersDetailsToShow = new Organisation().getDisplayItems();
  documentations: Documentation[] = [];
  documentationsDetailsToShow = new Documentation().getDisplayItems();

  status = [
    {num: 0, name: 'Active'},
    {num: 1, name: 'Inactive'}
  ];
  processor = 'Discovery';

  constructor(private log: LoggerService,
              private dpaService: DataProcessingAgreementService,
              private router: Router,
              private route: ActivatedRoute,
              private datePipe: DatePipe,
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
    this.dpa = {
      name : ''
    } as Dpa;
  }

  load(uuid: string) {
    this.dpaService.getDpa(uuid)
      .subscribe(result =>  {
          this.dpa = result;
          this.dpa.startDate = this.datePipe.transform(this.dpa.startDate,"yyyy-MM-dd");
          this.dpa.endDate = this.datePipe.transform(this.dpa.endDate,"yyyy-MM-dd");
        },
        error => this.log.error('The data processing agreement could not be loaded. Please try again.')
      );
  }

  purposeClicked(item: Purpose) {
    //TODO
  }

  deletePurposes() {
    //TODO
  }

  addPurpose() {
    //TODO
  }

  benefitClicked(item: Purpose) {
    //TODO
  }

  deleteBenefits() {
    //TODO
  }

  addBenefit() {
    //TODO
  }

  regionClicked(item: Region) {
    //TODO
  }

  deleteRegions() {
    //TODO
  }

  addRegion() {
    //TODO
  }

  publisherClicked(item: Organisation) {
    //TODO
  }

  deletePublishers() {
    //TODO
  }

  addPublisher() {
    //TODO
  }

  documentationClicked(item: Documentation) {
    //TODO
  }

  deleteDocumentations() {
    //TODO
  }

  addDocumentation() {
    //TODO
  }


  save(close: boolean) {
  }
}
