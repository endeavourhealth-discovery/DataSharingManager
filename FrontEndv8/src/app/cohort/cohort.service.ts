import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {Cohort} from './models/Cohort';
import {Dpa} from '../data-processing-agreement/models/Dpa';
import {Project} from "../project/models/Project";
import {Dsa} from "../data-sharing-agreement/models/Dsa";
import {Region} from "../region/models/Region";

@Injectable()
export class CohortService {

  constructor(private http: HttpClient) { }

  getAllCohorts(): Observable<Cohort[]> {
    const url = 'api/cohort';
    return this.http.get<Cohort[]>(url);
  }

  getCohortsBasedOnRegion(userId: string): Observable<Cohort[]> {
    const url = 'api/cohort';
    let params = new HttpParams();
    if (userId) params = params.append('userId', userId);
    return this.http.get<Cohort[]>(url, {params});
  }

  getCohort(uuid: string): Observable<Cohort> {
    const url = 'api/cohort';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Cohort>(url, {params});
  }

  saveCohort(cohort: Cohort): Observable<any> {
    const url = 'api/cohort';
    return this.http.post(url, cohort, { responseType: 'text' });
  }

  updateMappings(cohort: Cohort): Observable<any> {
    const url = 'api/cohort/updateMappings';
    return this.http.post(url, cohort, { responseType: 'text' });
  }

  deleteCohort(uuid: string[]): Observable<any> {
    const url = 'api/cohort';
    let params = new HttpParams();
    for (let ix in uuid) {
      params = params.append('uuids', uuid[ix]);
    }
    return this.http.delete<any>(url, {params});
  }

  search(searchData: string): Observable<Cohort[]> {
    const url = 'api/cohort';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<Cohort[]>(url, {params});
  }

  getLinkedDpas(uuid: string):  Observable<Dpa[]> {
    const url = 'api/cohort/dpas';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa[]>(url, {params});
  }

  getLinkedDsas(uuid: string):  Observable<Dsa[]> {
    const url = 'api/cohort/dsas';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>(url, {params});
  }

  getLinkedProjects(uuid: string):  Observable<Project[]> {
    const url = 'api/cohort/projects';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Project[]>(url, {params});
  }

  getLinkedRegions(uuid: string, userId: string):  Observable<Region[]> {
    const url = 'api/cohort/regions';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Region[]>(url,{params});
  }
}
