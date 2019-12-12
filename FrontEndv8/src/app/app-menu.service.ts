import {Injectable} from '@angular/core';
import {Routes} from '@angular/router';
import {AbstractMenuProvider, MenuOption} from 'dds-angular8';
import {OrganisationComponent} from "./organisation/organisation/organisation.component";
import {OrganisationEditorComponent} from "./organisation/organisation-editor/organisation-editor.component";

@Injectable()
export class AppMenuService implements  AbstractMenuProvider {
  static getRoutes(): Routes {
    return [
      { path: '', redirectTo : '/organisations/organisations', pathMatch: 'full' }, // Default route
      { path: 'organisations/:mode', component: OrganisationComponent, data: {role: 'Viewer'}},
      { path: 'organisation/:id/:mode', component: OrganisationEditorComponent, data: {role: 'Viewer'}},
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
      {caption: 'Organisations', state: 'organisations/organisations', icon: 'library_books'},
      {caption: 'Services', state: 'organisations/services', icon: 'library_books'},
    ];
  }
}
