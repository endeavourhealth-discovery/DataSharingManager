import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {Organisation} from '../organisation/models/Organisation';
import {Region} from '../region/models/Region';
import {DataFlow} from '../data-flow/models/DataFlow';
import {Dsa} from './models/Dsa';
import {Marker} from '../region/models/Marker';
import {Project} from "../project/models/Project";
import {Purpose} from "src/app/models/Purpose";
import {Cohort} from "../cohort/models/Cohort";
import {DataSet} from "../data-set/models/Dataset";

@Injectable()
export class DataSharingAgreementService {

  constructor(private http: HttpClient) { }

  getAllDsas(userId: string): Observable<Dsa[]> {
    const url = 'api/dsa';
    let params = new HttpParams();
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Dsa[]>(url,{params});
  }

  getDsa(uuid: string): Observable<Dsa> {
    const url = 'api/dsa';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa>(url,{params});
  }

  saveDsa(dsa: Dsa): Observable<any> {
    const url = 'api/dsa';
    return this.http.post(url, dsa, { responseType: 'text' });
  }

  updateMappings(dsa: Dsa): Observable<any> {
    const url = 'api/dsa/updateMappings';
    return this.http.post(url, dsa, {responseType: 'text'});
  }

  deleteDsa(uuid: string[]): Observable<any> {
    const url = 'api/dsa';
    let params = new HttpParams();
    for (let ix in uuid) {
      params = params.append('uuids', uuid[ix]);
    }
    return this.http.delete<any>(url,{params});
  }

  search(searchData: string): Observable<Dsa[]> {
    const url = 'api/dsa';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<Dsa[]>(url,{params});
  }

  getLinkedDataFlows(uuid: string):  Observable<DataFlow[]> {
    const url = 'api/dsa/dataflows';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataFlow[]>(url,{params});
  }

  getLinkedRegions(uuid: string, userId: string):  Observable<Region[]> {
    const url = 'api/dsa/regions';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Region[]>(url,{params});
  }

  getPublishers(uuid: string):  Observable<Organisation[]> {
    const url = 'api/dsa/publishers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>(url,{params});
  }

  getSubscribers(uuid: string):  Observable<Organisation[]> {
    const url = 'api/dsa/subscribers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>(url,{params});
  }

  getPurposes(uuid: string):  Observable<Purpose[]> {
    const url = 'api/dsa/purposes';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Purpose[]>(url,{params});
  }

  getBenefits(uuid: string):  Observable<Purpose[]> {
    const url = 'api/dsa/benefits';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Purpose[]>(url,{params});
  }

  getSubscriberMarkers(uuid: string): Observable<Marker[]> {
    const url = 'api/dsa/subscriberMarkers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Marker[]>(url,{params});
  }

  getPublisherMarkers(uuid: string): Observable<Marker[]> {
    const url = 'api/dsa/publisherMarkers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Marker[]>(url,{params});
  }

  getProjects(uuid: string): Observable<Project[]> {
    const url = 'api/dsa/projects';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Project[]>(url,{params});
  }

  getLinkedCohorts(uuid: string):  Observable<Cohort[]> {
    const url = 'api/dsa/cohorts';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Cohort[]>(url, {params});
  }

  getLinkedDataSets(uuid: string):  Observable<DataSet[]> {
    const url = 'api/dsa/dataSets';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataSet[]>(url, {params});
  }

}
