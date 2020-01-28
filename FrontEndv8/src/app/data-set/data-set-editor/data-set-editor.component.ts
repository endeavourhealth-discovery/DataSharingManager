import {Component, OnInit, ViewChild} from '@angular/core';
import {DataSet} from "../models/Dataset";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DataSetService} from "../data-set.service";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {MatDialog} from "@angular/material/dialog";
import {CohortDialogComponent} from "../../cohort/cohort-dialog/cohort-dialog.component";
import {DataSetDialogComponent} from "../data-set-dialog/data-set-dialog.component";

@Component({
  selector: 'app-data-set-editor',
  templateUrl: './data-set-editor.component.html',
  styleUrls: ['./data-set-editor.component.scss']
})
export class DataSetEditorComponent implements OnInit {

  @ViewChild('dpaTable', { static: false }) dpaTable: GenericTableComponent;

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
              private userManagerService: UserManagerService,
              public dialog: MatDialog) {
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
    MessageBoxDialogComponent.open(this.dialog, 'Remove DPA', 'Are you sure you want to remove DPA(s)?',
      'Remove DPA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dpaTable.selection.selected.length; i++) {
              let org = this.dpaTable.selection.selected[i];
              this.processingAgreements.forEach( (item, index) => {
                if(item === org) this.processingAgreements.splice(index,1);
              });
            }
            this.dpaTable.updateRows();
            this.updateDPAMapping();
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addDPAs() {
    const dialogRef = this.dialog.open(DataProcessingAgreementPickerComponent, {
      width: '800px',
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dpa of result) {
        if (!this.processingAgreements.some(x => x.uuid === dpa.uuid)) {
          this.processingAgreements.push(dpa);
          this.dpaTable.updateRows();
        }
        this.updateDPAMapping();
      }
    })
  }

  updateDPAMapping() {
    // Populate Data Processing Agreements before save
    this.dataset.dpas = {};
    for (const idx in this.processingAgreements) {
      let dpa: Dpa = this.processingAgreements[idx];
      this.dataset.dpas[dpa.uuid] = dpa.name;
    }

    this.dataSetService.updateMappings(this.dataset)
      .subscribe(saved => {
          this.dataset.uuid = saved;
          this.log.success('Data Set saved successfully');
        },
        error => this.log.error('The Data Set could not be saved. Please try again.')
      );
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

  close() {
    window.history.back();
  }

  editDataset() {
    const dialogRef = this.dialog.open(DataSetDialogComponent, {
      width: '800px',
      data: {mode: 'edit', uuid: this.dataset.uuid },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dataset = result;
        this.save(false);
      }
    });
  }
}
