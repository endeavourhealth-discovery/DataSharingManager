import { BrowserModule } from '@angular/platform-browser';
import { NgModule} from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {KeycloakService} from 'eds-angular4/dist/keycloak/keycloak.service';
import {keycloakHttpFactory} from 'eds-angular4/dist/keycloak/keycloak.http';
import {Http, HttpModule, RequestOptions, XHRBackend} from '@angular/http';
import {LayoutComponent} from 'eds-angular4/dist/layout/layout.component';
import {LayoutModule, MenuService } from 'eds-angular4';
import {AppMenuService} from './app-menu.service';
import {SettingsComponent} from './settings/settings/settings.component';
import {SettingsModule} from './settings/settings.module';
import {ConceptModellerComponent} from './concept-modeller/concept-modeller/concept-modeller.component';
import {ConceptModellerModule} from './concept-modeller/concept-modeller.module';
import {OrganisationModule} from './organisation/organisation.module';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import { OrganisationComponent } from './organisation/organisation/organisation.component';
import { OrganisationOverviewComponent } from './organisation/organisation-overview/organisation-overview.component';
import {ToastModule} from 'ng2-toastr/ng2-toastr';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
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
import {RegionModule} from "./region/region.module";
import {DataFlowModule} from "./data-flow/data-flow.module";
import {DataSharingAgreementModule} from "./data-sharing-agreement/data-sharing-agreement.module";
import {DataProcessingAgreementModule} from "./data-processing-agreement/data-processing-agreement.module";
import {DataSharingSummaryModule} from "./data-sharing-summary/data-sharing-summary.module";

export class DummyComponent {}

const appRoutes: Routes = [
  { path: 'conceptModeller', component: ConceptModellerComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'organisationOverview', component: OrganisationOverviewComponent},
  { path: 'organisations', component: OrganisationComponent},
  { path: 'organisation/:id/:mode', component: OrganisationEditorComponent},
  { path: 'regions', component: RegionComponent},
  { path: 'region/:id/:mode', component: RegionEditorComponent},
  { path: 'dataFlows', component: DataFlowComponent},
  { path: 'dataFlow/:id/:mode', component: DataFlowEditorComponent},
  { path: 'dsas', component: DataSharingAgreementComponent},
  { path: 'dsa/:id/:mode', component: DataSharingAgreementEditorComponent},
  { path: 'dpas', component: DataProcessingAgreementComponent},
  { path: 'dpa/:id/:mode', component: DataProcessingAgreementEditorComponent},
  { path: 'dataSharingSummaries', component: DataSharingSummaryComponent},
  { path: 'dataSharingSummary/:id/:mode', component: DataSharingSummaryEditorComponent}
];

@NgModule({
  declarations: [],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpModule,
    LayoutModule,
    SettingsModule,
    ConceptModellerModule,
    OrganisationModule,
    RegionModule,
    DataFlowModule,
    DataSharingAgreementModule,
    DataProcessingAgreementModule,
    DataSharingSummaryModule,
    RouterModule.forRoot(appRoutes),
    NgbModule.forRoot(),
    ToastModule.forRoot()
  ],
  providers: [
    KeycloakService,
    { provide: Http, useFactory: keycloakHttpFactory, deps: [XHRBackend, RequestOptions, KeycloakService] },
    { provide: MenuService, useClass : AppMenuService }
  ],
  bootstrap: [LayoutComponent]
})
export class AppModule { }
