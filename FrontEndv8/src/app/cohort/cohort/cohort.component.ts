import {Component, OnInit, ViewChild} from '@angular/core';
import {Cohort} from '../models/Cohort';
import {CohortService} from '../cohort.service';
import {Router} from '@angular/router';
import {LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {GenericTableComponent} from "../../generic-table/generic-table/generic-table.component";
import {MatDialog} from "@angular/material/dialog";
import {CohortDialogComponent} from "../cohort-dialog/cohort-dialog.component";
import {UserProject} from "dds-angular8/user-manager";

@Component({
  selector: 'app-cohort',
  templateUrl: './cohort.component.html',
  styleUrls: ['./cohort.component.css']
})
export class CohortComponent implements OnInit {
  cohorts: Cohort[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  cohortDetailsToShow = new Cohort().getDisplayItems();
  loadingComplete = false;

  public activeProject: UserProject;

  @ViewChild('cohortsTable', {static: false}) cohortsTable: GenericTableComponent;

  constructor(private cohortService: CohortService,
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

    this.getCohorts();
  }

  roleChanged() {
    if (this.activeProject.applicationPolicyAttributes.find(x => x.applicationAccessProfileName == 'Admin') != null) {
      this.allowEdit = true;
    } else {
      this.allowEdit = false;
    }
  }

  getCohorts() {
    this.loadingComplete = false;
    this.cohortService.getAllCohorts()
      .subscribe(
        result => {
          this.cohorts = result;
          this.loadingComplete = true;
        },
            error => {
          this.log.error('The cohorts could not be loaded. Please try again.');
          this.loadingComplete = true;
        }
      );
  }

  add() {
    const dialogRef = this.dialog.open(CohortDialogComponent, {
      data: {mode: 'add', uuid: ''},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/cohort', result.uuid, 'edit']);
      }
    });
  }

  itemClicked(cohort: Cohort) {
    this.router.navigate(['/cohort', cohort.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete cohorts', 'Are you sure you want to delete cohorts?',
        'Delete cohorts', 'Cancel')
        .subscribe(
            (result) => {
              if(result) {
                let ids = [];
                for (var i = 0; i < this.cohortsTable.selection.selected.length; i++) {
                  let cohort = this.cohortsTable.selection.selected[i];
                  this.cohorts.forEach( (item, index) => {
                    if(item === cohort) {
                      this.cohorts.splice(index,1);
                      this.cohortsTable.updateRows();
                      ids.push(item.uuid);
                    }
                  });
                }
                this.cohortService.deleteCohort(ids).subscribe(
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
