import { Injectable } from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Organisation} from '../organisation/models/Organisation';
import {Region} from './models/Region';
import {Dsa} from '../data-sharing-agreement/models/Dsa';
import {URLSearchParams, Http} from '@angular/http';
import {Marker} from "./models/Marker";
import {Dpa} from "../data-processing-agreement/models/Dpa";

@Injectable()
export class RegionService {

  constructor(private http: Http) { }

  getAllRegions(userId: string): Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    if (userId != null) {
      params.set('userId', userId);
    }
    return vm.http.get('api/region', { search : params })
      .map((response) => response.json());
  }

  getRegion(uuid: string): Observable<Region> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/region', { search : params })
      .map((response) => response.json());
  }

  getRegionOrganisations(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/region/organisations', { search : params })
      .map((response) => response.json());
  }

  getParentRegions(uuid: string, userId: string):  Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    if (userId != null) {
      params.set('userId', userId);
    }
    return vm.http.get('api/region/parentRegions', { search : params })
      .map((response) => response.json());
  }

  getChildRegions(uuid: string):  Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/region/childRegions', { search : params })
      .map((response) => response.json());
  }

  getSharingAgreements(uuid: string):  Observable<Dsa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/region/sharingAgreements', { search : params })
      .map((response) => response.json());
  }

  getProcessingAgreements(uuid: string):  Observable<Dpa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/region/processingAgreements', { search : params })
      .map((response) => response.json());
  }

  saveRegion(region: Region): Observable<any> {
    const vm = this;
    return vm.http.post('api/region', region)
      .map((response) => response.text());
  }

  deleteRegion(uuid: string): Observable<any> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.delete('api/region', { search : params })
      .map((response) => response.text());
  }

  search(searchData: string): Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('searchData', searchData);
    return vm.http.get('api/region', { search : params })
      .map((response) => response.json());
  }

  getAPIKey(): Observable<any> {
    const vm = this;
    return vm.http.get('api/region/getApiKey')
      .map((response) => response.json());
  }

  getOrganisationMarkers(uuid: string): Observable<Marker[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/region/markers', { search : params })
      .map((response) => response.json());
  }

}
