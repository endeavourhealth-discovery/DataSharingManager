import {Injectable} from '@angular/core';
import {URLSearchParams, Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {Organisation} from './models/Organisation';
import {Region} from '../region/models/Region';
import {Address} from './models/Address';
import {Marker} from '../region/models/Marker';
import {OrganisationManagerStatistics} from './models/OrganisationManagerStatistics';
import {FileUpload} from './models/FileUpload';
import {Dpa} from "../data-processing-agreement/models/Dpa";
import {Dsa} from "../data-sharing-agreement/models/Dsa";

@Injectable()
export class OrganisationService  {

  constructor(private http: Http) {  }

  getOrganisation(uuid: string): Observable<Organisation> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation', { search : params })
      .map((response) => response.json());
  }

  getOrganisationRegions(uuid: string):  Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/regions', { search : params })
      .map((response) => response.json());
  }

  getChildOrganisations(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/childOrganisations', { search : params })
      .map((response) => response.json());
  }

  getParentOrganisations(uuid: string, isService: number):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    params.set('isService', isService.toString());
    return vm.http.get('api/organisation/parentOrganisations', { search : params })
      .map((response) => response.json());
  }

  getServices(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/services', { search : params })
      .map((response) => response.json());
  }

  getDPAPublishing(uuid: string):  Observable<Dpa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/dpasPublishing', { search : params })
      .map((response) => response.json());
  }

  getDSAPublishing(uuid: string):  Observable<Dsa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/dsasPublishing', { search : params })
      .map((response) => response.json());
  }

  getDSASubscribing(uuid: string):  Observable<Dsa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/dsasSubscribing', { search : params })
      .map((response) => response.json());
  }

  getOrganisationAddresses(uuid: string):  Observable<Address[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/organisation/addresses', { search : params })
      .map((response) => response.json());
  }

  saveOrganisation(organisation: Organisation): Observable<any> {
    const vm = this;
    return vm.http.post('api/organisation', organisation)
      .map((response) => response.text());
  }

  deleteOrganisation(uuid: string): Observable<any> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.delete('api/organisation', { search : params })
      .map((response) => response.json());
  }

  search(searchData: string, searchType: string,
         pageNumber: number = 1, pageSize: number = 20,
         orderColumn: string = 'name', descending: boolean = false ): Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('searchData', searchData);
    params.set('searchType', searchType);
    params.set('pageNumber', pageNumber.toString());
    params.set('pageSize', pageSize.toString());
    params.set('orderColumn', orderColumn);
    params.set('descending', descending.toString());
    return vm.http.get('api/organisation', { search : params })
      .map((response) => response.json());
  }

  getUpdatedBulkOrganisations(): Observable<any> {
    const vm = this;
    return vm.http.get('api/organisation/editedBulks')
      .map((response) => response.json());
  }

  getConflictedOrganisations(): Observable<any> {
    const vm = this;
    return vm.http.get('api/organisation/conflicts')
      .map((response) => response.json());
  }

  deleteBulks(): Observable<any> {
    const vm = this;
    return vm.http.delete('api/organisation/deleteBulks')
      .map((response) => response.json());
  }

  startUpload(): Observable<any> {
    const vm = this;
    return vm.http.get('api/organisation/startUpload', {withCredentials : true})
      .map((response) => response.text());
  }

  endUpload(): Observable<any> {
    const vm = this;
    return vm.http.get('api/organisation/endUpload')
      .map((response) => response.text());
  }

  uploadCsv(fileToUpload: FileUpload): Observable<any> {
    const vm = this;
    return vm.http.post('api/organisation/upload', fileToUpload);
  }

  getStatistics(type: string): Observable<OrganisationManagerStatistics[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('type', type);
    return vm.http.get('api/organisation/statistics', { search : params })
      .map((response) => response.json());
  }

  getTotalCount(expression: string, searchType: string): Observable<number> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('expression', expression);
    params.set('searchType', searchType);
    return vm.http.get('api/organisation/searchCount', { search : params })
      .map((response) => response.json());
  }

}
