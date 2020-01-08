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
import {DataExchangeComponent} from './data-exchange/data-exchange/data-exchange.component';
//import {DataExchangeEditorComponent} from './data-exchange/data-exchange-editor/data-exchange-editor.component';
import {DataFlowComponent} from './data-flow/data-flow/data-flow.component';
//import {DataFlowEditorComponent} from './data-flow/data-flow-editor/data-flow-editor.component';
import {DataSetComponent} from './data-set/data-set/data-set.component';
import {DataSetEditorComponent} from './data-set/data-set-editor/data-set-editor.component';
import {DataProcessingAgreementComponent} from './data-processing-agreement/data-processing-agreement/data-processing-agreement.component';
//import {DataProcessingAgreementEditorComponent} from './data-processing-agreement/data-processing-agreement-editor/data-processing-agreement-editor.component';
import {DataSharingAgreementComponent} from './data-sharing-agreement/data-sharing-agreement/data-sharing-agreement.component';
import {DataSharingSummaryComponent} from './data-sharing-summary/data-sharing-summary/data-sharing-summary.component';
import {DataProcessingAgreementEditorComponent} from "./data-processing-agreement/data-processing-agreement-editor/data-processing-agreement-editor.component";
//import {DataSharingSummaryEditorComponent} from './data-sharing-summary/data-sharing-summary-editor/data-sharing-summary-editor.component';

@Injectable()
export class AppMenuService implements  AbstractMenuProvider {
  static getRoutes(): Routes {
    return [
      { path: '', redirectTo : '/organisations/organisations', pathMatch: 'full' }, // Default route
      { path: 'organisations/:mode', component: OrganisationComponent, data: {role: 'Viewer'}},
      { path: 'organisation/:id/:mode', component: OrganisationEditorComponent, data: {role: 'Viewer'}},
      { path: 'regions', component: RegionComponent, data: {role: 'Viewer'}},
      { path: 'region/:id/:mode', component: RegionEditorComponent, data: {role: 'Viewer'}},
      { path: 'value-sets', component: ValueSetsComponent, data: {role: 'Viewer'}},
      { path: 'cohorts', component: CohortComponent, data: {role: 'Viewer'}},
      { path: 'cohort/:id/:mode', component: CohortEditorComponent, data: {role: 'Viewer'}},
      { path: 'dataExchanges', component: DataExchangeComponent, data: {role: 'Viewer'}},
      //{ path: 'dataExchange/:id/:mode', component: DataExchangeEditorComponent, data: {role: 'Viewer'}},
      { path: 'dataFlows', component: DataFlowComponent, data: {role: 'Viewer'}},
      //{ path: 'dataFlow/:id/:mode', component: DataFlowEditorComponent, data: {role: 'Viewer'}},
      { path: 'dataSets', component: DataSetComponent, data: {role: 'Viewer'}},
      { path: 'dataSet/:id/:mode', component: DataSetEditorComponent, data: {role: 'Viewer'}},
      { path: 'dpas', component: DataProcessingAgreementComponent, data: {role: 'Viewer'}},
      { path: 'dpa/:id/:mode', component: DataProcessingAgreementEditorComponent, data: {role: 'Viewer'}},
      { path: 'dsas', component: DataSharingAgreementComponent, data: {role: 'Viewer'}},
      //{ path: 'dsa/:id/:mode', component: DataSharingAgreementEditorComponent, data: {role: 'Viewer'}},
      { path: 'dataSharingSummaries', component: DataSharingSummaryComponent, data: {role: 'Viewer'}},
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
      {caption: 'Regions', state: 'regions', icon: 'library_books'},
      {caption: 'Organisations', state: 'organisations/organisations', icon: 'library_books'},
      {caption: 'Services', state: 'organisations/services', icon: 'library_books'},
      {caption: 'Data sharing agreements', state: 'dsas', icon: 'library_books'},
      {caption: 'Data processing agreements', state: 'dpas', icon: 'library_books'},
      {caption: 'Data sets', state: 'dataSets', icon: 'library_books'},
      {caption: 'Cohorts', state: 'cohorts', icon: 'library_books'},
    ];
  }
}