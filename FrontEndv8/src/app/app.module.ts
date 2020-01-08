import { NgModule, DoBootstrap, ApplicationRef } from '@angular/core';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import {AppMenuService} from './app-menu.service';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {AbstractMenuProvider, LayoutComponent, LayoutModule, LoggerModule, SecurityModule, UserManagerModule} from 'dds-angular8';
import {OrganisationModule} from "./organisation/organisation.module";
import {SchedulerModule} from "./scheduler/scheduler.module";
import {ValueSetsModule} from "./value-sets/value-sets.module";
import {GenericTableModule} from "./generic-table/generic-table.module";
import {RegionModule} from "./region/region.module";
import {CohortModule} from "./cohort/cohort.module";
import {DataExchangeModule} from "./data-exchange/data-exchange.module";
import {DataFlowModule} from "./data-flow/data-flow.module";
import {DataSetModule} from "./data-set/data-set.module";
import {DataProcessingAgreementModule} from "./data-processing-agreement/data-processing-agreement.module";
import {DataSharingAgreementModule} from "./data-sharing-agreement/data-sharing-agreement.module";
import {DataSharingSummaryModule} from "./data-sharing-summary/data-sharing-summary.module";
import {PurposeModule} from "./purpose/purpose.module";


const keycloakService = new KeycloakService();

@NgModule({
  imports: [
    KeycloakAngularModule,
    HttpClientModule,

    OrganisationModule,
    RegionModule,
    SchedulerModule,
    PurposeModule,
    ValueSetsModule,
    CohortModule,
    DataExchangeModule,
    DataFlowModule,
    DataSetModule,
    DataProcessingAgreementModule,
    DataSharingAgreementModule,
    DataSharingSummaryModule,

    LayoutModule,
    SecurityModule,
    LoggerModule,
    UserManagerModule,
    GenericTableModule,

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
      .init({config: 'public/wellknown/authconfigraw', initOptions: {onLoad: 'login-required'}})
      .then((authenticated) => {
        if (authenticated)
          appRef.bootstrap(LayoutComponent);
      })
      .catch(error => console.error('[ngDoBootstrap] init Keycloak failed', error));
  }
}