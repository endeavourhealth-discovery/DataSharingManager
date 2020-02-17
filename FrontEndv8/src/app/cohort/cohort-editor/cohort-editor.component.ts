import {Component, OnInit, ViewChild} from '@angular/core';
import {Cohort} from "../models/Cohort";
import {CohortService} from '../cohort.service';
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from '@angular/router';
import {Dpa} from '../../data-processing-agreement/models/Dpa';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {MatDialog} from "@angular/material/dialog";
import {CohortDialogComponent} from "../cohort-dialog/cohort-dialog.component";
import {Dsa} from "../../data-sharing-agreement/models/Dsa";
import {Project} from "../../project/models/Project";
import {DataSharingAgreementPickerComponent} from "../../data-sharing-agreement/data-sharing-agreement-picker/data-sharing-agreement-picker.component";
import {ProjectPickerComponent} from "../../project/project-picker/project-picker.component";

@Component({
  selector: 'app-cohort-editor',
  templateUrl: './cohort-editor.component.html',
  styleUrls: ['./cohort-editor.component.css']
})
export class CohortEditorComponent implements OnInit {
  private paramSubscriber: any;
  cohort: Cohort = <Cohort>{};
  dpas: Dpa[] = [];
  dsas: Dsa[] = [];
  projects: Project[] = [];
  allowEdit = false;
  public activeProject: UserProject;
  dpaDetailsToShow = new Dpa().getDisplayItems();
  dsaDetailsToShow = new Dsa().getDisplayItems();
  projectDetailsToShow = new Project().getDisplayItems();

  @ViewChild('dpaTable', {static: false}) dpaTable: GenericTableComponent;
  @ViewChild('dsaTable', {static: false}) dsaTable: GenericTableComponent;
  @ViewChild('projectTable', {static: false}) projectTable: GenericTableComponent;

  constructor(private log: LoggerService,
              private cohortService: CohortService,
              private router: Router,
              private route: ActivatedRoute,
              private userManagerNotificationService: UserManagerService,
              public dialog: MatDialog) {
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
          this.getLinkedDsas();
          this.getLinkedProjects();
        },
        error => this.log.error('The cohort could not be loaded. Please try again.')
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
          this.log.success('Cohort saved successfully');
          if (close) {
            this.close();
          }
        },
        error => this.log.error('The cohort could not be saved. Please try again.')
      );
  }

  editCohort() {
    const dialogRef = this.dialog.open(CohortDialogComponent, {
      data: {mode: 'edit', uuid: this.cohort.uuid },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.cohort = result;
        this.log.success('Cohort saved successfully');
      }
    });
  }

  close() {
    window.history.back();
  }

  clearMappings() {
    this.cohort.dpas = null;
    this.cohort.dsas = null;
    this.cohort.projects = null;
  }

  updateMappings(type: string) {
    this.cohortService.updateMappings(this.cohort)
      .subscribe(saved => {
            this.cohort.uuid = saved;
            if (type == 'Processing agreements') {
              this.getLinkedDpas();
            } else if (type == 'Sharing agreements') {
              this.getLinkedDsas()
            } else if (type == 'Projects') {
              this.getLinkedProjects()
            }
            this.log.success(type + ' updated successfully.');
          },
          error => this.log.error('The cohort could not be saved. Please try again.')
      );
  }

  private getLinkedDpas() {
    this.cohortService.getLinkedDpas(this.cohort.uuid)
      .subscribe(
        result => {
          this.dpas = result;
          this.dpaTable.updateRows();
        },
        error => this.log.error('The associated data processing agreements could not be loaded. Please try again.')
      );
  }

  private getLinkedDsas() {
    this.cohortService.getLinkedDsas(this.cohort.uuid)
      .subscribe(
        result => {
          this.dsas = result;
          this.dsaTable.updateRows();
        },
        error => this.log.error('The associated data sharing agreements could not be loaded. Please try again.')
      );
  }

  private getLinkedProjects() {
    this.cohortService.getLinkedProjects(this.cohort.uuid)
      .subscribe(
        result => {
          this.projects = result;
          this.projectTable.updateRows();
        },
        error => this.log.error('The associated data projects could not be loaded. Please try again.')
      );
  }

  dpaClicked(item: Dpa) {
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
              this.dpas.forEach( (item, index) => {
                if(item === org) this.dpas.splice(index,1);
              });
            }
            this.clearMappings();
            this.cohort.dpas = {};
            for (const idx in this.dpas) {
              const dpa: Dpa = this.dpas[idx];
              this.cohort.dpas[dpa.uuid] = dpa.name;
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
            this.cohort.dsas = {};
            for (const idx in this.dsas) {
              const dsa: Dsa = this.dsas[idx];
              this.cohort.dsas[dsa.uuid] = dsa.name;
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
            this.cohort.projects = {};
            for (const idx in this.projects) {
              const project: Project = this.projects[idx];
              this.cohort.projects[project.uuid] = project.name;
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
      data: {fromRegion: false, existing: this.dpas},
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      for (let dpa of result) {
        if (!this.dpas.some(x => x.uuid === dpa.uuid)) {
          this.dpas.push(dpa);
        }
      }
      this.clearMappings();
      this.cohort.dpas = {};
      for (const idx in this.dpas) {
        const dpa: Dpa = this.dpas[idx];
        this.cohort.dpas[dpa.uuid] = dpa.name;
      }
      this.updateMappings('Processing agreements');
    })
  }

  addDSAs() {
    const dialogRef = this.dialog.open(DataSharingAgreementPickerComponent, {
      minWidth: '50vw',
      data: {allowMultiple: true, existing: this.dsas}
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
      this.cohort.dsas = {};
      for (const idx in this.dsas) {
        const dsa: Dsa = this.dsas[idx];
        this.cohort.dsas[dsa.uuid] = dsa.name;
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
      this.cohort.projects = {};
      for (const idx in this.projects) {
        const project: Project = this.projects[idx];
        this.cohort.projects[project.uuid] = project.name;
      }
      this.updateMappings('Projects');
    })
  }
}
