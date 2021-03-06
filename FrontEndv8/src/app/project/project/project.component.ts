import {Component, OnInit, ViewChild} from '@angular/core';
import {Project} from "../models/Project";
import {Router} from "@angular/router";
import {GenericTableComponent, LoggerService, MessageBoxDialogComponent, UserManagerService} from "dds-angular8";
import {ProjectService} from "../project.service";
import {MatDialog} from "@angular/material/dialog";
import {ProjectDialogComponent} from "../project-dialog/project-dialog.component";
import {UserProject} from "dds-angular8/user-manager";

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit {

  @ViewChild('projectsTable', {static: false}) projectsTable: GenericTableComponent;

  projects: Project[];
  pageNumber = 1;
  pageSize = 20;
  orderColumn = 'name';
  descending = false;
  allowEdit = false;
  projectDetailsToShow = new Project().getDisplayItems();
  superUser = false;
  userId: string;
  loadingComplete = false;

  public activeProject: UserProject;

  constructor(private projectService: ProjectService,
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
    this.getProjects(this.userId);
  }

  getProjects(userId: string) {
    this.loadingComplete = false;
    this.projectService.getAllProjects(userId)
      .subscribe(
        result => {
          this.projects = result;
          this.loadingComplete = true;
        },
        error => {
          this.log.error('The projects could not be loaded. Please try again.');
          this.loadingComplete = true;
        }
      );
  }

  add() {
    const dialogRef = this.dialog.open(ProjectDialogComponent, {
      data: {mode: 'add', uuid: ''},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/project', result.uuid, 'edit']);
        this.log.success('Project saved');
      }
    });
  }

  edit(item: Project) {
    this.router.navigate(['/project', item.uuid, 'edit']);
  }

  itemClicked(item: Project) {
    this.router.navigate(['/project', item.uuid, 'edit']);
  }

  delete() {
    MessageBoxDialogComponent.open(this.dialog, 'Delete projects', 'Are you sure you want to delete projects?',
      'Delete projects', 'Cancel')
      .subscribe(
        (result) => {
          if(result) {
            let ids = [];
            for (var i = 0; i < this.projectsTable.selection.selected.length; i++) {
              let project = this.projectsTable.selection.selected[i];
              this.projects.forEach( (item, index) => {
                if(item === project) {
                  this.projects.splice(index,1);
                  this.projectsTable.updateRows();
                  ids.push(item.uuid);
                }
              });
            }
            this.projectService.deleteProject(ids).subscribe(
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
