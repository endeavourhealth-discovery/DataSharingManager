import {Component, OnInit, ViewChild} from '@angular/core';
import {Cohort} from "../models/Cohort";
import {CohortService} from '../cohort.service';
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {ActivatedRoute, Router} from '@angular/router';
import {Dpa} from '../../data-processing-agreement/models/Dpa';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {DataProcessingAgreementPickerComponent} from "../../data-processing-agreement/data-processing-agreement-picker/data-processing-agreement-picker.component";
import {MatDialog} from "@angular/material/dialog";

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

  @ViewChild('dpaTable', {static: false}) dpaTable: GenericTableComponent;

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
        },
        error => this.log.error('The Cohort could not be loaded. Please try again.'/*, error, 'Load cohort'*/)
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
        error => this.log.error('The Cohort could not be saved. Please try again.')
      );
  }

  close() {
    window.history.back();
  }

  private getLinkedDpas() {
    this.cohortService.getLinkedDpas(this.cohort.uuid)
      .subscribe(
        result => {
          this.dpas = result;
        },
        error => this.log.error('The associated data processing agreements could not be loaded. Please try again.'/*, error, 'Load associated data processing agreements'*/)
      );
  }

  dpaClicked(item: Dpa) {
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
              this.dpas.forEach( (item, index) => {
                if(item === org) this.dpas.splice(index,1);
              });
            }
            this.dpaTable.updateRows();
            this.log.success('Remove successful.');
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
      for (let dpa of result) {
        if (!this.dpas.some(x => x.uuid === dpa.uuid)) {
          this.dpas.push(dpa);
          this.dpaTable.updateRows();
        }
      }
    })
  }
}
