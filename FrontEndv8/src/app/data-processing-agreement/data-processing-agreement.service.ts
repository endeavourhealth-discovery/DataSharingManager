import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {DataFlow} from '../data-flow/models/DataFlow';
import {Dpa} from './models/Dpa';
import {DataSet} from '../data-set/models/Dataset';
import {Cohort} from '../cohort/models/Cohort';
import {Organisation} from '../organisation/models/Organisation';
import {Marker} from '../region/models/Marker';
import {Region} from "../region/models/Region";
import { Purpose } from "src/app/models/Purpose";

@Injectable()
export class DataProcessingAgreementService {

  constructor(private http: HttpClient) { }

  getAllDpas(userId: string): Observable<Dpa[]> {
    const url = 'api/dpa';
    let params = new HttpParams();
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Dpa[]>(url,{params});
  }

  getDpa(uuid: string): Observable<Dpa> {
    const url = 'api/dpa';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa>(url,{params});
  }

  saveDpa(dpa: Dpa): Observable<any> {
    const url = 'api/dpa';
    return this.http.post(url, dpa, {responseType: 'text'});
  }

  deleteDpa(uuid: string[]): Observable<any> {
    const url = 'api/dpa';
    let params = new HttpParams();
    for (let ix in uuid) {
      params = params.append('uuids', uuid[ix]);
    }
    return this.http.delete<any>(url,{params});
  }

  search(searchData: string): Observable<Dpa[]> {
    const url = 'api/dpa';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<Dpa[]>(url,{params});
  }

  getLinkedRegions(uuid: string, userId: string):  Observable<Region[]> {
    const url = 'api/dpa/regions';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Region[]>(url,{params});
  }

  makeAPICall(uuid: string, path: string):  Observable<any[]> {
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<any[]>(path, {params});
  }

  getLinkedDataFlows(uuid: string):  Observable<DataFlow[]> {

    return this.makeAPICall(uuid, 'api/dpa/dataflows');
  }

  getLinkedCohorts(uuid: string):  Observable<Cohort[]> {

    return this.makeAPICall(uuid, 'api/dpa/cohorts');
  }

  getLinkedDataSets(uuid: string):  Observable<DataSet[]> {

    return this.makeAPICall(uuid, 'api/dpa/datasets');
  }

  getPublishers(uuid: string):  Observable<Organisation[]> {

    return this.makeAPICall(uuid, 'api/dpa/publishers');
  }

  getSubscriberMarkers(uuid: string): Observable<Marker[]> {

    return this.makeAPICall(uuid, 'api/dpa/subscriberMarkers');
  }

  getPublisherMarkers(uuid: string): Observable<Marker[]> {

    return this.makeAPICall(uuid, 'api/dpa/publisherMarkers');
  }

  getPurposes(uuid: string):  Observable<Purpose[]> {

    return this.makeAPICall(uuid, 'api/dpa/purposes');
  }

  getBenefits(uuid: string):  Observable<Purpose[]> {

    return this.makeAPICall(uuid, 'api/dpa/benefits');
  }

}
