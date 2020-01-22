import {Component, OnInit, ViewChild} from '@angular/core';
import {Dpa} from '../models/Dpa';
import {DataProcessingAgreementService} from '../data-processing-agreement.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-data-processing-agreement',
  templateUrl: './data-processing-agreement.component.html',
  styleUrls: ['./data-processing-agreement.component.css']
})
export class DataProcessingAgreementComponent implements OnInit {
  dpas: Dpa[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dpaDetailsToShow = new Dpa().getDisplayItems();
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  @ViewChild('dpasTable', {static: false}) dpasTable: GenericTableComponent;

  constructor(private dpaService: DataProcessingAgreementService,
              private router: Router,
              private userManagerNotificationService: UserManagerService,
              private log: LoggerService,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.userManagerNotificationService.onProjectChange.subscribe(active => {
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
    this.getDpas(this.userId);
  }

  getDpas(userId: string) {
    this.loadingComplete = false;
    this.dpaService.getAllDpas(userId)
      .subscribe(
        result => {
          this.dpas = result;
          this.loadingComplete = true;
        },
            error => {
          this.log.error('The data processing agreements could not be loaded. Please try again.');
          this.loadingComplete = true;
        }
      );
  }

  add() {
    this.router.navigate(['/dpa', 1, 'add']);
  }

  edit(item: Dpa) {
    this.router.navigate(['/dpa', item.uuid, 'edit']);
  }

  itemClicked(dpa: Dpa) {
    this.router.navigate(['/dpa', dpa.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete DPA', 'Are you sure you want to delete DPA(s)?',
      'Delete DPA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            let ids = [];
            for (var i = 0; i < this.dpasTable.selection.selected.length; i++) {
              let dpa = this.dpasTable.selection.selected[i];
              this.dpas.forEach( (item, index) => {
                if(item === dpa) {
                  this.dpas.splice(index,1);
                  this.dpasTable.updateRows();
                  ids.push(item.uuid);
                }
              });
            }
            this.dpaService.deleteDpa(ids).subscribe(
              () => {
                this.log.success('Delete successful.');
              }
            );
          } else {
            this.log.success('Delete cancelled.')
          }
        });
  }

  close() {
    this.router.navigate(['/overview']);
  }

}
