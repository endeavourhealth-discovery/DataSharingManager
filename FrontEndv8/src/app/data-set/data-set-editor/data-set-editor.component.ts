import {Component, OnInit, ViewChild} from '@angular/core';
import {DataSet} from "../models/Dataset";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from "@angular/router";
import {DataSetService} from "../data-set.service";
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {Dpa} from "../../data-processing-agreement/models/Dpa";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {MatDialog} from "@angular/material/dialog";
import {DataSetDialogComponent} from "../data-set-dialog/data-set-dialog.component";
import {Project} from "../../project/models/Project";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {ProjectPickerComponent} from "../../project/project-picker/project-picker.component";

@Component({
  selector: 'app-data-set-editor',
  templateUrl: './data-set-editor.component.html',
  styleUrls: ['./data-set-editor.component.scss']
})
export class DataSetEditorComponent implements OnInit {

  @ViewChild('dpaTable', { static: false }) dpaTable: GenericTableComponent;
  @ViewChild('dsaTable', {static: false}) dsaTable: GenericTableComponent;
  @ViewChild('projectTable', {static: false}) projectTable: GenericTableComponent;

  dataset: DataSet;
  processingAgreements: Dpa[] = [];
  dsas: Dsa[] = [];
  projects: Project[] = [];
  processingAgreementsDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();
  projectDetailsToShow = new Project().getDisplayItems();
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
        this.create();
        break;
      case 'edit':
        this.load(itemUuid);
        break;
    }
  }

  create() {
    this.dataset = {
      name : ''
    } as DataSet;
  }

  load(uuid: string) {
    this.dataSetService.getDataSet(uuid)
      .subscribe(result =>  {
          this.dataset = result;
          this.getProcessingAgreements();
          this.getLinkedDsas();
          this.getLinkedProjects();
        },
        error => this.log.error('The data set could not be loaded. Please try again.')
      );
  }

  private getProcessingAgreements() {
    this.dataSetService.getLinkedDpas(this.dataset.uuid)
      .subscribe(
        result => {
          this.processingAgreements = result;
          this.dpaTable.updateRows();
        },
        error => this.log.error('The associated data processing agreements could not be loaded. Please try again.')
      );
  }
  private getLinkedDsas() {
    this.dataSetService.getLinkedDsas(this.dataset.uuid)
      .subscribe(
        result => {
          this.dsas = result;
          this.dsaTable.updateRows();
        },
        error => this.log.error('The associated data sharing agreements could not be loaded. Please try again.')
      );
  }

  private getLinkedProjects() {
    this.dataSetService.getLinkedProjects(this.dataset.uuid)
      .subscribe(
        result => {
          this.projects = result;
          this.projectTable.updateRows();
        },
        error => this.log.error('The associated data projects could not be loaded. Please try again.')
      );
  }

  processingAgreementClicked(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  dsaClicked(item: Dpa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  projectClicked(item: Dpa) {
    this.router.navigate(['/project', item.uuid, 'edit']);
  }

  deleteDPAs() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove processing agreements', 'Are you sure you want to remove processing agreements?',
      'Remove processing agreements', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dpaTable.selection.selected.length; i++) {
              let org = this.dpaTable.selection.selected[i];
              this.processingAgreements.forEach( (item, index) => {
                if(item === org) this.processingAgreements.splice(index,1);
              });
            }
            this.clearMappings();
            this.dataset.dpas = {};
            for (const idx in this.processingAgreements) {
              const dpa: Dpa = this.processingAgreements[idx];
              this.dataset.dpas[dpa.uuid] = dpa.name;
            }
            this.updateMappings('Processing agreements');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteDSAs() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove sharing agreements', 'Are you sure you want to remove sharing agreements?',
      'Remove sharing agreements', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.dsaTable.selection.selected.length; i++) {
              let org = this.dsaTable.selection.selected[i];
              this.dsas.forEach( (item, index) => {
                if(item === org) this.dsas.splice(index,1);
              });
            }
            this.clearMappings();
            this.dataset.dsas = {};
            for (const idx in this.dsas) {
              const dsa: Dsa = this.dsas[idx];
              this.dataset.dsas[dsa.uuid] = dsa.name;
            }
            this.updateMappings('Sharing agreements');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  deleteProjects() {
    MessageBoxDialogComponent.open(this.dialog, 'Remove projects', 'Are you sure you want to remove projects?',
      'Remove projects', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            for (var i = 0; i < this.projectTable.selection.selected.length; i++) {
              let org = this.projectTable.selection.selected[i];
              this.projects.forEach( (item, index) => {
                if(item === org) this.projects.splice(index,1);
              });
            }
            this.clearMappings();
            this.dataset.projects = {};
            for (const idx in this.projects) {
              const project: Project = this.projects[idx];
              this.dataset.projects[project.uuid] = project.name;
            }
            this.updateMappings('Projects');
          } else {
            this.log.success('Remove cancelled.')
          }
        },
      );
  }

  addDPAs() {
    const dialogRef = this.dialog.open(DataProcessingAgreementPickerComponent, {
      minWidth: '50vw',
      data: {fromRegion: false, existing: this.processingAgreements},
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dpa of result) {
        if (!this.processingAgreements.some(x => x.uuid === dpa.uuid)) {
          this.processingAgreements.push(dpa);
        }
      }
      this.clearMappings();
      this.dataset.dpas = {};
      for (const idx in this.processingAgreements) {
        const dpa: Dpa = this.processingAgreements[idx];
        this.dataset.dpas[dpa.uuid] = dpa.name;
      }
      this.updateMappings('Processing agreements');
    })
  }

  addDSAs() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      minWidth: '50vw',
      data: { allowMultiple: true, existing: this.dsas }
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dsa of result) {
        if (!this.dsas.some(x => x.uuid === dsa.uuid)) {
          this.dsas.push(dsa);
        }
      }
      this.clearMappings();
      this.dataset.dsas = {};
      for (const idx in this.dsas) {
        const dsa: Dsa = this.dsas[idx];
        this.dataset.dsas[dsa.uuid] = dsa.name;
      }
      this.updateMappings('Sharing agreements');
    })
  }

  addProjects() {
    const dialogRef = this.dialog.open(ProjectPickerComponent, {
      minWidth: '50vw',
      data: {uuid: '', limit: 0, userId : this.activeProject.userId, existing: this.projects},
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let proj of result) {
        if (!this.projects.some(x => x.uuid === proj.uuid)) {
          this.projects.push(proj);
        }
      }
      this.clearMappings();
      this.dataset.projects = {};
      for (const idx in this.projects) {
        const project: Project = this.projects[idx];
        this.dataset.projects[project.uuid] = project.name;
      }
      this.updateMappings('Projects');
    })
  }

  clearMappings() {
    this.dataset.dpas = null;
    this.dataset.dsas = null;
    this.dataset.projects = null;
  }

  updateMappings(type: string) {
    this.dataSetService.updateMappings(this.dataset)
      .subscribe(saved => {
          this.dataset.uuid = saved;
          this.log.success(type + ' updated successfully.');
          this.refreshMappings(type);
        },
        error => {
          this.log.error('The data set could not be saved. Please try again.');
          this.refreshMappings(type);
        }
      );
  }

  refreshMappings(type: string) {
    if (type == 'Processing agreements') {
      this.getProcessingAgreements();
    } else if (type == 'Sharing agreements') {
      this.getLinkedDsas()
    } else if (type == 'Projects') {
      this.getLinkedProjects()
    }
  }

  save(close: boolean) {
    this.dataSetService.saveDataSet(this.dataset)
      .subscribe(saved => {
          this.dataset.uuid = saved;
          this.log.success('Data set saved successfully.');
          if (close) { window.history.back(); }
        },
        error => this.log.error('The data set could not be saved. Please try again.')
      );
  }

  close() {
    window.history.back();
  }

  editDataset() {
    const dialogRef = this.dialog.open(DataSetDialogComponent, {
      data: {mode: 'edit', uuid: this.dataset.uuid },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dataset = result;
        this.log.success('Data set saved successfully.');
      }
    });
  }
}
