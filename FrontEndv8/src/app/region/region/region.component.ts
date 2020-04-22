import {Component, OnInit, ViewChild} from '@angular/core';
import {RegionService} from '../region.service';
import {Router} from '@angular/router';
import {Region} from "../models/Region";
import {Organisation} from "../../organisation/models/Organisation";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {MatDialog} from "@angular/material/dialog";
import {RegionDialogComponent} from "../region-dialog/region-dialog.component";
import {UserProject} from "dds-angular8/user-manager";

@Component({
  selector: 'app-region',
  templateUrl: './region.component.html',
  styleUrls: ['./region.component.css']
})
export class RegionComponent implements OnInit {
  organisations: Organisation[];
  regions: Region[] = [];
  allowEdit = false;
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  regionDetailsToShow = new Region().getDisplayItems();
  @ViewChild('regionsTable', {static: false}) regionsTable: GenericTableComponent;

  constructor(private regionService: RegionService,
              private log: LoggerService,
              private router: Router,
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

    this.getRegions(this.userId);
  }

  getRegions(userId: string) {
    this.loadingComplete = false;
    this.regionService.getAllRegions(userId)
      .subscribe(
        result => {
          this.regions = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The regions could not be loaded. Please try again.');
          this.loadingComplete = true;
        }
      );
  }

  add() {
    const dialogRef = this.dialog.open(RegionDialogComponent, {
      data: {mode: 'add', uuid: ''},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/region', result.uuid, 'edit']);
        this.log.success('Region saved.');
      }
    });
  }

  edit(item: Region) {
    this.router.navigate(['/region', item.uuid, 'edit']);
  }

  itemClicked(region: Region) {
    this.router.navigate(['/region', region.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete regions', 'Are you sure you want to delete regions?',
      'Delete regions', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            let ids = [];
            for (var i = 0; i < this.regionsTable.selection.selected.length; i++) {
              let region = this.regionsTable.selection.selected[i];
              this.regions.forEach( (item, index) => {
                if(item === region) {
                  this.regions.splice(index,1);
                  this.regionsTable.updateRows();
                  ids.push(item.uuid);
                }
              });
            }
            console.log(ids);
            this.regionService.deleteRegion(ids).subscribe(
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
    this.router.navigate(['overview']);
  }

}
