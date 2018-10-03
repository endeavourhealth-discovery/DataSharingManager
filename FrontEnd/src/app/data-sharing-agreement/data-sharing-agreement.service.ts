import { Injectable } from '@angular/core';
import {URLSearchParams, Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {Organisation} from '../organisation/models/Organisation';
import {Purpose} from './models/Purpose';
import {Region} from '../region/models/Region';
import {DataFlow} from '../data-flow/models/DataFlow';
import {Dsa} from './models/Dsa';
import {Marker} from '../region/models/Marker';
import {Project} from "../project/models/Project";

@Injectable()
export class DataSharingAgreementService {

  constructor(private http: Http) { }


  getAllDsas(): Observable<Dsa[]> {
    const vm = this;
    return vm.http.get('api/dsa')
      .map((response) => response.json());
  }

  getDsa(uuid: string): Observable<Dsa> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa', { search : params })
      .map((response) => response.json());
  }

  saveDsa(dsa: Dsa): Observable<any> {
    const vm = this;
    return vm.http.post('api/dsa', dsa)
      .map((response) => response.text());
  }

  deleteDsa(uuid: string): Observable<any> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.delete('api/dsa', { search : params })
      .map((response) => response.text());
  }

  search(searchData: string): Observable<Dsa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('searchData', searchData);
    return vm.http.get('api/dsa', { search : params })
      .map((response) => response.json());
  }

  getLinkedDataFlows(uuid: string):  Observable<DataFlow[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/dataflows', { search : params })
      .map((response) => response.json());
  }

  getLinkedRegions(uuid: string):  Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/regions', { search : params })
      .map((response) => response.json());
  }

  getPublishers(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/publishers', { search : params })
      .map((response) => response.json());
  }

  getSubscribers(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/subscribers', { search : params })
      .map((response) => response.json());
  }

  getPurposes(uuid: string):  Observable<Purpose[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/purposes', { search : params })
      .map((response) => response.json());
  }

  getBenefits(uuid: string):  Observable<Purpose[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/benefits', { search : params })
      .map((response) => response.json());
  }

  getSubscriberMarkers(uuid: string): Observable<Marker[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/subscriberMarkers', { search : params })
      .map((response) => response.json());
  }

  getPublisherMarkers(uuid: string): Observable<Marker[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/publisherMarkers', { search : params })
      .map((response) => response.json());
  }

  getProjects(uuid: string): Observable<Project[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dsa/projects', { search : params })
      .map((response) => response.json());
  }

}
