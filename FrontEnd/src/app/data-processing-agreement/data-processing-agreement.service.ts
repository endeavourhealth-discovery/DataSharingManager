import { Injectable } from '@angular/core';
import {URLSearchParams, Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {DataFlow} from '../data-flow/models/DataFlow';
import {Dpa} from './models/Dpa';
import {DataSet} from '../data-set/models/Dataset';
import {Cohort} from '../cohort/models/Cohort';
import {Organisation} from '../organisation/models/Organisation';
import {Marker} from '../region/models/Marker';
import {Purpose} from "../data-sharing-agreement/models/Purpose";
import {Region} from "../region/models/Region";

@Injectable()
export class DataProcessingAgreementService {

  constructor(private http: Http) { }

  getAllDpas(): Observable<Dpa[]> {
    const vm = this;
    return vm.http.get('api/dpa')
      .map((response) => response.json());
  }

  getDpa(uuid: string): Observable<Dpa> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dpa', { search : params })
      .map((response) => response.json());
  }

  saveDpa(dpa: Dpa): Observable<any> {
    const vm = this;
    return vm.http.post('api/dpa', dpa)
      .map((response) => response.text());
  }

  deleteDpa(uuid: string): Observable<any> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.delete('api/dpa', { search : params })
      .map((response) => response.text());
  }

  search(searchData: string): Observable<Dpa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('searchData', searchData);
    return vm.http.get('api/dpa', { search : params })
      .map((response) => response.json());
  }

  getLinkedDataFlows(uuid: string):  Observable<DataFlow[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/dataflows');
  }

  getLinkedRegions(uuid: string):  Observable<Region[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/dpa/regions', { search : params })
      .map((response) => response.json());
  }

  getLinkedCohorts(uuid: string):  Observable<Cohort[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/cohorts');
  }

  getLinkedDataSets(uuid: string):  Observable<DataSet[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/datasets');
  }

  getPublishers(uuid: string):  Observable<Organisation[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/publishers');
  }

  getSubscriberMarkers(uuid: string): Observable<Marker[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/subscriberMarkers');
  }

  getPublisherMarkers(uuid: string): Observable<Marker[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/publisherMarkers');
  }

  getPurposes(uuid: string):  Observable<Purpose[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/purposes');
  }

  getBenefits(uuid: string):  Observable<Purpose[]> {
    const vm = this;

    return vm.makeAPICall(uuid, 'api/dpa/benefits');
  }

  makeAPICall(uuid: string, path: string):  Observable<any[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get(path, { search : params })
      .map((response) => response.json());
  }

}
