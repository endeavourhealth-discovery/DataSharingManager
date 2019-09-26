import {Injectable} from '@angular/core';
import {AbstractMenuProvider } from 'eds-angular4';
import {MenuOption} from 'eds-angular4/dist/layout/models/MenuOption';
import {Routes} from '@angular/router';
import {OrganisationOverviewComponent} from './organisation/organisation-overview/organisation-overview.component';
import {DataSharingSummaryOverviewComponent} from './data-sharing-summary/data-sharing-summary-overview/data-sharing-summary-overview.component';
import {OrganisationComponent} from './organisation/organisation/organisation.component';
import {OrganisationEditorComponent} from './organisation/organisation-editor/organisation-editor.component';
import {RegionComponent} from './region/region/region.component';
import {RegionEditorComponent} from './region/region-editor/region-editor.component';
import {DataFlowComponent} from './data-flow/data-flow/data-flow.component';
import {DataFlowEditorComponent} from './data-flow/data-flow-editor/data-flow-editor.component';
import {DataSharingAgreementComponent} from './data-sharing-agreement/data-sharing-agreement/data-sharing-agreement.component';
import {DataSharingAgreementEditorComponent} from './data-sharing-agreement/data-sharing-agreement-editor/data-sharing-agreement-editor.component';
import {DataProcessingAgreementComponent} from './data-processing-agreement/data-processing-agreement/data-processing-agreement.component';
import {DataProcessingAgreementEditorComponent} from './data-processing-agreement/data-processing-agreement-editor/data-processing-agreement-editor.component';
import {DataSharingSummaryComponent} from './data-sharing-summary/data-sharing-summary/data-sharing-summary.component';
import {DataSharingSummaryEditorComponent} from './data-sharing-summary/data-sharing-summary-editor/data-sharing-summary-editor.component';
import {CohortComponent} from './cohort/cohort/cohort.component';
import {CohortEditorComponent} from './cohort/cohort-editor/cohort-editor.component';
import {DataSetComponent} from './data-set/data-set/data-set.component';
import {DataSetEditorComponent} from './data-set/data-set-editor/data-set-editor.component';
import {MySharingOverviewComponent} from "./my-sharing/my-sharing-overview/my-sharing-overview.component";
import {DataExchangeComponent} from "./data-exchange/data-exchange/data-exchange.component";
import {DataExchangeEditorComponent} from "./data-exchange/data-exchange-editor/data-exchange-editor.component";
import {ProjectComponent} from "./project/project/project.component";
import {ProjectEditorComponent} from "./project/project-editor/project-editor.component";
import {ConfigComponent} from "./config/config/config.component";
import {ReportsComponent} from "./reporting/reports/reports.component";

@Injectable()
export class AppMenuService implements  AbstractMenuProvider  {
  static getRoutes(): Routes {
    return [
      { path: '', redirectTo : 'mySharingOverview', pathMatch: 'full' }, // Default route
      { path: 'overview', component: OrganisationOverviewComponent},
      { path: 'sharingOverview', component: DataSharingSummaryOverviewComponent},
      { path: 'organisations', component: OrganisationComponent},
      { path: 'organisation/:id/:mode', component: OrganisationEditorComponent},
      { path: 'regions', component: RegionComponent},
      { path: 'region/:id/:mode', component: RegionEditorComponent},
      { path: 'dataFlows', component: DataFlowComponent},
      { path: 'dataFlow/:id/:mode', component: DataFlowEditorComponent},
      { path: 'dataExchanges', component: DataExchangeComponent},
      { path: 'dataExchange/:id/:mode', component: DataExchangeEditorComponent},
      { path: 'dsas', component: DataSharingAgreementComponent},
      { path: 'dsa/:id/:mode', component: DataSharingAgreementEditorComponent},
      { path: 'dpas', component: DataProcessingAgreementComponent},
      { path: 'dpa/:id/:mode', component: DataProcessingAgreementEditorComponent},
      { path: 'dataSharingSummaries', component: DataSharingSummaryComponent},
      { path: 'dataSharingSummary/:id/:mode', component: DataSharingSummaryEditorComponent},
      { path: 'cohorts', component: CohortComponent},
      { path: 'cohort/:id/:mode', component: CohortEditorComponent},
      { path: 'dataSets', component: DataSetComponent},
      { path: 'dataSet/:id/:mode', component: DataSetEditorComponent},
      { path: 'mySharingOverview', component: MySharingOverviewComponent},
      { path: 'projects', component: ProjectComponent},
      { path: 'project/:id/:mode', component: ProjectEditorComponent},
      { path: 'configuration', component: ConfigComponent},
      { path: 'reporting', component: ReportsComponent},
    ];
  }

  useUserManagerForRoles(): boolean {
    return true;
  }

  getClientId(): string {
    return 'eds-dsa-manager';
  }
  getApplicationTitle(): string {
    return 'Data Sharing Manager';
  }
  getMenuOptions(): MenuOption[] {
    return [
      {caption: 'My agreements', state: 'mySharingOverview', icon: 'fa fa-user', role: 'Viewer'},
      {caption: 'Overview', state: 'overview', icon: 'fa fa-hospital-o', role: 'Viewer'},
      {caption: 'Configuration', state: 'configuration', icon: 'fa-cogs', role: 'Config'},
      {caption: 'Reporting', state: 'reporting', icon: 'fa-line-chart', role: 'Config'}
    ];
  }
}
