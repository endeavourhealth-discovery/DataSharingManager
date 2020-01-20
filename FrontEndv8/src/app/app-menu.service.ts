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
//import {DataExchangeComponent} from './data-exchange/data-exchange/data-exchange.component';
//import {DataExchangeEditorComponent} from './data-exchange/data-exchange-editor/data-exchange-editor.component';
//import {DataFlowComponent} from './data-flow/data-flow/data-flow.component';
//import {DataFlowEditorComponent} from './data-flow/data-flow-editor/data-flow-editor.component';
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
//import {DataSharingSummaryComponent} from './data-sharing-summary/data-sharing-summary/data-sharing-summary.component';
//import {DataSharingSummaryEditorComponent} from './data-sharing-summary/data-sharing-summary-editor/data-sharing-summary-editor.component';

@Injectable()
export class AppMenuService implements  AbstractMenuProvider {
  static getRoutes(): Routes {
    return [
      { path: '', redirectTo : 'mySharingOverview', pathMatch: 'full' }, // Default route
      { path: 'organisations/:mode', component: OrganisationComponent, data: {role: 'Viewer'}},
      { path: 'organisation/:id/:mode', component: OrganisationEditorComponent, data: {role: 'Viewer'}},
      { path: 'regions', component: RegionComponent, data: {role: 'Viewer'}},
      { path: 'region/:id/:mode', component: RegionEditorComponent, data: {role: 'Viewer'}},
      { path: 'value-sets', component: ValueSetsComponent, data: {role: 'Viewer'}},
      { path: 'cohorts', component: CohortComponent, data: {role: 'Viewer'}},
      { path: 'cohort/:id/:mode', component: CohortEditorComponent, data: {role: 'Viewer'}},
      //{ path: 'dataExchanges', component: DataExchangeComponent, data: {role: 'Viewer'}},
      //{ path: 'dataExchange/:id/:mode', component: DataExchangeEditorComponent, data: {role: 'Viewer'}},
      //{ path: 'dataFlows', component: DataFlowComponent, data: {role: 'Viewer'}},
      //{ path: 'dataFlow/:id/:mode', component: DataFlowEditorComponent, data: {role: 'Viewer'}},
      { path: 'dataSets', component: DataSetComponent, data: {role: 'Viewer'}},
      { path: 'dataSet/:id/:mode', component: DataSetEditorComponent, data: {role: 'Viewer'}},
      { path: 'dpas', component: DataProcessingAgreementComponent, data: {role: 'Viewer'}},
      { path: 'dpa/:id/:mode', component: DataProcessingAgreementEditorComponent, data: {role: 'Viewer'}},
      { path: 'dsas', component: DataSharingAgreementComponent, data: {role: 'Viewer'}},
      { path: 'dsa/:id/:mode', component: DataSharingAgreementEditorComponent, data: {role: 'Viewer'}},
      { path: 'projects', component: ProjectComponent, data: {role: 'Viewer'}},
      { path: 'project/:id/:mode', component: ProjectEditorComponent, data: {role: 'Viewer'}},
      { path: 'audit', component: AuditComponent, data: {role: 'Viewer'}},
      { path: 'mySharingOverview', component: MySharingOverviewComponent, data: {role: 'Viewer'}},
      { path: 'reporting', component: ReportsComponent, data: {role: 'Admin'}},
      //{ path: 'dataSharingSummaries', component: DataSharingSummaryComponent, data: {role: 'Viewer'}},
      //{ path: 'dataSharingSummary/:id/:mode', component: DataSharingSummaryEditorComponent, data: {role: 'Viewer'}},
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
      {caption: 'My sharing', state: 'mySharingOverview', icon: 'fas fa-handshake'},
      {caption: 'Regions', state: 'regions', icon: 'fas fa-globe-europe'},
      {caption: 'Organisations', state: 'organisations/organisations', icon: 'fas fa-hospital'},
      {caption: 'Services', state: 'organisations/services', icon: 'fas fa-clinic-medical'},
      {caption: 'Data sharing agreements', state: 'dsas', icon: 'fas fa-file-export'},
      {caption: 'Data processing agreements', state: 'dpas', icon: 'fas fa-file-import'},
      {caption: 'Data sets', state: 'dataSets', icon: 'fas fa-layer-group'},
      {caption: 'Cohorts', state: 'cohorts', icon: 'fas fa-users-class'},
      {caption: 'Projects', state: 'projects', icon: 'fas fa-folders'},
      {caption: 'Reporting', state: 'reporting', icon: 'fas fa-analytics'},
      {caption: 'Audit', state: 'audit', icon: 'fas fa-history'}
    ];
  }
}
