import { NgModule, DoBootstrap, ApplicationRef } from '@angular/core';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import {AppMenuService} from './app-menu.service';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {AbstractMenuProvider, LayoutComponent, LayoutModule, LoggerModule, SecurityModule, UserManagerModule} from 'dds-angular8';
import {OrganisationModule} from "./organisation/organisation.module";
import {SchedulerModule} from "./scheduler/scheduler.module";

const keycloakService = new KeycloakService();

@NgModule({
  imports: [
    KeycloakAngularModule,
    HttpClientModule,

    LayoutModule,
    SecurityModule,
    LoggerModule,
    UserManagerModule,
    OrganisationModule,
    SchedulerModule,

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
