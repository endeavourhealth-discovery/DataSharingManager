import {Component, OnInit, ViewChild} from '@angular/core';
import {Dsa} from '../models/Dsa';
import {DataSharingAgreementService} from '../data-sharing-agreement.service';
import {Router} from '@angular/router';
import {UserProject} from "dds-angular8/lib/user-manager/models/UserProject";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {DataSharingAgreementDialogComponent} from "../data-sharing-agreement-dialog/data-sharing-agreement-dialog.component";

@Component({
  selector: 'app-data-sharing-agreement',
  templateUrl: './data-sharing-agreement.component.html',
  styleUrls: ['./data-sharing-agreement.component.css']
})
export class DataSharingAgreementComponent implements OnInit {
  dsas: Dsa[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  dsaDetailsToShow = new Dsa().getDisplayItems();
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  @ViewChild('dsasTable', {static: false}) dsasTable: GenericTableComponent;

  constructor(private dsaService: DataSharingAgreementService,
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
    this.getDsas(this.userId);
  }

  getDsas(userId: string) {
    this.loadingComplete = false;
    console.log(userId);
    this.dsaService.getAllDsas(userId)
      .subscribe(
        result => {
          this.dsas = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The data sharing agreements could not be loaded. Please try again.'/*, error, 'Load data sharing agreements'*/);
          this.loadingComplete = true;
        }
      );
  }

  add() {
    const dialogRef = this.dialog.open(DataSharingAgreementDialogComponent, {
      width: '80vw',
      data: {mode: 'add', uuid: ''},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/dsa', result.uuid, 'edit']);
        this.log.success('Data sharing agreement saved.');
      }
    });
  }

  edit(item: Dsa) {
    this.router.navigate(['/dsa', item.uuid, 'edit']);
  }

  itemClicked(dsa: Dsa) {
    this.router.navigate(['/dsa', dsa.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete DSA', 'Are you sure you want to delete DSA(s)?',
      'Delete DSA', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            let ids = [];
            for (var i = 0; i < this.dsasTable.selection.selected.length; i++) {
              let dsa = this.dsasTable.selection.selected[i];
              this.dsas.forEach( (item, index) => {
                if(item === dsa) {
                  this.dsas.splice(index,1);
                  this.dsasTable.updateRows();
                  ids.push(item.uuid);
                }
              });
            }
            this.dsaService.deleteDsa(ids).subscribe(
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
