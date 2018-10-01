import { BrowserModule } from '@angular/platform-browser';
import { NgModule} from '@angular/core';
import { RouterModule } from '@angular/router';

import {KeycloakService} from 'eds-angular4/dist/keycloak/keycloak.service';
import {keycloakHttpFactory} from 'eds-angular4/dist/keycloak/keycloak.http';
import {Http, HttpModule, RequestOptions, XHRBackend} from '@angular/http';
import {LayoutComponent} from 'eds-angular4/dist/layout/layout.component';
import {LayoutModule, AbstractMenuProvider, UserManagerNotificationService} from 'eds-angular4';
import {AppMenuService} from './app-menu.service';
import {OrganisationModule} from './organisation/organisation.module';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ToastModule} from 'ng2-toastr/ng2-toastr';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RegionModule} from './region/region.module';
import {DataFlowModule} from './data-flow/data-flow.module';
import {DataSharingAgreementModule} from './data-sharing-agreement/data-sharing-agreement.module';
import {DataProcessingAgreementModule} from './data-processing-agreement/data-processing-agreement.module';
import {DataSharingSummaryModule} from './data-sharing-summary/data-sharing-summary.module';
import {CohortModule} from './cohort/cohort.module';
import {DataSetModule} from './data-set/data-set.module';
import {MySharingModule} from "./my-sharing/my-sharing.module";
import {DataExchangeModule} from "./data-exchange/data-exchange.module";
import {ProjectModule} from "./project/project.module";

@NgModule({
  declarations: [],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpModule,
    LayoutModule,
    CohortModule,
    DataSetModule,
    OrganisationModule,
    RegionModule,
    DataFlowModule,
    DataExchangeModule,
    DataSharingAgreementModule,
    DataProcessingAgreementModule,
    DataSharingSummaryModule,
    MySharingModule,
    ProjectModule,
    RouterModule.forRoot(AppMenuService.getRoutes(), {useHash: true}),
    NgbModule.forRoot(),
    ToastModule.forRoot()
  ],
  providers: [
    KeycloakService,
    { provide: Http, useFactory: keycloakHttpFactory, deps: [XHRBackend, RequestOptions, KeycloakService, AbstractMenuProvider, UserManagerNotificationService] },
    { provide: AbstractMenuProvider, useClass : AppMenuService }
  ],
  bootstrap: [LayoutComponent]
})
export class AppModule { }
