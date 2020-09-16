import { NgModule, DoBootstrap, ApplicationRef } from '@angular/core';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import {AppMenuService} from './app-menu.service';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {
  AbstractMenuProvider,
  LayoutComponent,
  LayoutModule,
  LoggerModule,
  SecurityModule,
  UserManagerModule,
  DialogsModule,
  GenericTableModule
} from 'dds-angular8';
import {OrganisationModule} from "./organisation/organisation.module";
import {SchedulerModule} from "./scheduler/scheduler.module";
import {ValueSetsModule} from "./value-sets/value-sets.module";
import {RegionModule} from "./region/region.module";
import {CohortModule} from "./cohort/cohort.module";
import {DataExchangeModule} from "./data-exchange/data-exchange.module";
import {DataFlowModule} from "./data-flow/data-flow.module";
import {DataSetModule} from "./data-set/data-set.module";
import {DataProcessingAgreementModule} from "./data-processing-agreement/data-processing-agreement.module";
import {DataSharingAgreementModule} from "./data-sharing-agreement/data-sharing-agreement.module";
import {DataSharingSummaryModule} from "./data-sharing-summary/data-sharing-summary.module";
import {PurposeModule} from "./purpose/purpose.module";
import {DocumentationModule} from "./documentation/documentation.module";
import {ProjectModule} from "./project/project.module";
import {AuditModule} from "./audit/audit.module";
import {MySharingModule} from "./my-sharing/my-sharing.module";
import {ReportingModule} from "./reporting/reporting.module";
import {GoogleMapsViewerModule} from "./google-maps-viewer/google-maps-viewer.module";


const keycloakService = new KeycloakService();

@NgModule({
  imports: [
    KeycloakAngularModule,
    HttpClientModule,

    OrganisationModule,
    RegionModule,
    SchedulerModule,
    PurposeModule,
    DocumentationModule,
    ValueSetsModule,
    CohortModule,
    DataExchangeModule,
    DataFlowModule,
    DataSetModule,
    DataProcessingAgreementModule,
    DataSharingAgreementModule,
    DataSharingSummaryModule,
    ProjectModule,
    AuditModule,

    LayoutModule,
    SecurityModule,
    LoggerModule,
    UserManagerModule,
    GenericTableModule,
    MySharingModule,
    ReportingModule,
    DialogsModule,
    GoogleMapsViewerModule,

    RouterModule.forRoot(AppMenuService.getRoutes(), {useHash: true}),
  ],
  providers: [
    { provide: AbstractMenuProvider, useClass : AppMenuService },
    { provide: KeycloakService, useValue: keycloakService }
  ]
})
export class AppModule implements DoBootstrap {
  ngDoBootstrap(appRef: ApplicationRef) {
    keycloakService
      .init({config: 'public/wellknown/authconfigraw', initOptions: {onLoad: 'login-required', 'checkLoginIframe':false}})
      .then((authenticated) => {
        if (authenticated)
          appRef.bootstrap(LayoutComponent);
      })
      .catch(error => console.error('[ngDoBootstrap] init Keycloak failed', error));
  }
}
