import {Injectable} from '@angular/core';
import {Routes} from '@angular/router';
import {AbstractMenuProvider, MenuOption} from 'dds-angular8';
import {OrganisationComponent} from "./organisation/organisation/organisation.component";
import {OrganisationEditorComponent} from "./organisation/organisation-editor/organisation-editor.component";
import {ValueSetsComponent} from "./value-sets/value-sets/value-sets.component";
import {RegionComponent} from "./region/region/region.component";
import {RegionEditorComponent} from "./region/region-editor/region-editor.component";
import {CohortComponent} from "./cohort/cohort/cohort.component";
import {CohortEditorComponent} from "./cohort/cohort-editor/cohort-editor.component";
import {DataSetComponent} from './data-set/data-set/data-set.component';
import {DataSetEditorComponent} from './data-set/data-set-editor/data-set-editor.component';
import {DataProcessingAgreementComponent} from './data-processing-agreement/data-processing-agreement/data-processing-agreement.component';
import {DataProcessingAgreementEditorComponent} from './data-processing-agreement/data-processing-agreement-editor/data-processing-agreement-editor.component';
import {DataSharingAgreementComponent} from './data-sharing-agreement/data-sharing-agreement/data-sharing-agreement.component';
import {DataSharingAgreementEditorComponent} from "./data-sharing-agreement/data-sharing-agreement-editor/data-sharing-agreement-editor.component";
import {ProjectComponent} from "./project/project/project.component";
import { ProjectEditorComponent } from "src/app/project/project-editor/project-editor.component";
import {AuditComponent} from "./audit/audit/audit.component";
import {MySharingOverviewComponent} from "./my-sharing/my-sharing-overview/my-sharing-overview.component";
import {ReportsComponent} from "./reporting/reports/reports.component";

@Injectable()
export class AppMenuService implements  AbstractMenuProvider {
  static getRoutes(): Routes {
    return [
      { path: '', redirectTo : 'mySharingOverview', pathMatch: 'full' }, // Default route
      { path: 'organisations/:mode', component: OrganisationComponent, data: {role: 'Viewer', helpContext: '${mode}'}},
      { path: 'organisation/:id/:mode', component: OrganisationEditorComponent, data: {role: 'Viewer', helpContext: '${mode}#edit'}},
      { path: 'regions', component: RegionComponent, data: {role: 'Viewer', helpContext: 'Regions#view'}},
      { path: 'region/:id/:mode', component: RegionEditorComponent, data: {role: 'Viewer', helpContext: 'Regions#edit'}},
      { path: 'value-sets', component: ValueSetsComponent, data: {role: 'Viewer', helpContext: 'Regions#view'}},
      { path: 'cohorts', component: CohortComponent, data: {role: 'Viewer', helpContext: 'Cohorts#view'}},
      { path: 'cohort/:id/:mode', component: CohortEditorComponent, data: {role: 'Viewer', helpContext: 'Cohorts#edit'}},
      { path: 'dataSets', component: DataSetComponent, data: {role: 'Viewer', helpContext: 'Data-sets#view'}},
      { path: 'dataSet/:id/:mode', component: DataSetEditorComponent, data: {role: 'Viewer', helpContext: 'Data-sets#edit'}},
      { path: 'dpas', component: DataProcessingAgreementComponent, data: {role: 'Viewer', helpContext: 'Data-processing-agreements#view'}},
      { path: 'dpa/:id/:mode', component: DataProcessingAgreementEditorComponent, data: {role: 'Viewer', helpContext: 'Data-processing-agreements#edit'}},
      { path: 'dsas', component: DataSharingAgreementComponent, data: {role: 'Viewer', helpContext: 'Data-sharing-agreements#view'}},
      { path: 'dsa/:id/:mode', component: DataSharingAgreementEditorComponent, data: {role: 'Viewer', helpContext: 'Data-sharing-agreements#edit'}},
      { path: 'projects', component: ProjectComponent, data: {role: 'Viewer', helpContext: 'Projects#view'}},
      { path: 'project/:id/:mode', component: ProjectEditorComponent, data: {role: 'Viewer', helpContext: 'Projects#edit'}},
      { path: 'audit', component: AuditComponent, data: {role: 'Viewer', helpContext: 'Audit#view'}},
      { path: 'mySharingOverview', component: MySharingOverviewComponent, data: {role: 'Viewer', helpContext: 'My-sharing'}},
      { path: 'reporting', component: ReportsComponent, data: {role: 'Admin', helpContext: 'Reporting#view'}},
    ];
  }

  getClientId(): string {
    return 'eds-dsa-manager';
  }

  getApplicationTitle(): string {
    return 'Data Sharing Manager';
  }

  getMenuOptions(): MenuOption[] {
    return [
      {caption: 'My sharing', state: 'mySharingOverview', icon: 'fas fa-handshake-alt'},
      {caption: 'Regions', state: 'regions', icon: 'fas fa-globe-europe'},
      {caption: 'Organisations', state: 'organisations/Organisations', icon: 'fas fa-hospital'},
      {caption: 'Services', state: 'organisations/Services', icon: 'fas fa-clinic-medical'},
      {caption: 'Data processing agreements', state: 'dpas', icon: 'fas fa-file-import'},
      {caption: 'Data sharing agreements', state: 'dsas', icon: 'fas fa-file-export'},
      {caption: 'Data sets', state: 'dataSets', icon: 'fas fa-layer-group'},
      {caption: 'Cohorts', state: 'cohorts', icon: 'fas fa-users-class'},
      {caption: 'Projects', state: 'projects', icon: 'fas fa-folders'},
      {caption: 'Reporting', state: 'reporting', icon: 'fas fa-analytics'},
      {caption: 'Audit', state: 'audit', icon: 'fas fa-history'}
    ];
  }
}
