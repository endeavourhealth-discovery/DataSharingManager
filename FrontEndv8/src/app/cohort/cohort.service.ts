import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {Cohort} from './models/Cohort';
import {Dpa} from '../data-processing-agreement/models/Dpa';

@Injectable()
export class CohortService {

  constructor(private http: HttpClient) { }

  getAllCohorts(): Observable<Cohort[]> {
    const url = 'api/cohort';
    return this.http.get<Cohort[]>(url);
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

  updateDPAMapping(cohort: Cohort): Observable<any> {
    const url = 'api/cohort/updateDPAMapping';
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

}
