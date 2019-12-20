import { Injectable } from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Region} from "../models/Region";
import {Organisation} from "../models/Organisation";
import {Dsa} from "../models/Dsa";
import {Dpa} from "../models/Dpa";
import {Marker} from "../models/Marker";

@Injectable()
export class RegionService {

  constructor(private http: HttpClient) { }

  getAllRegions(userId: string): Observable<Region[]> {

    let params = new HttpParams();
    if (userId) params = params.append('userId', userId);

    return this.http.get<Region[]>('api/region', { params });
  }

  getRegion(uuid: string): Observable<Region> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Region>('api/region', { params });
  }

  getRegionOrganisations(uuid: string):  Observable<Organisation[]> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>('api/region/organisations', { params });
  }

  getParentRegions(uuid: string, userId: string):  Observable<Region[]> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    if (userId) params = params.append('userId', userId);
    return this.http.get<Region[]>('api/region/parentRegions', { params });
  }

  getChildRegions(uuid: string):  Observable<Region[]> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Region[]>('api/region/childRegions', { params });
  }

  getSharingAgreements(uuid: string):  Observable<Dsa[]> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>('api/region/sharingAgreements', { params });
  }

  getProcessingAgreements(uuid: string):  Observable<Dpa[]> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa[]> ('api/region/processingAgreements', { params });
  }

  saveRegion(region: Region): Observable<any> {

    return this.http.post<any>('api/region', region);
  }

  deleteRegion(uuid: string): Observable<any> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.delete<any>('api/region', { params });
  }

  search(searchData: string): Observable<Region[]> {

    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
    return this.http.get<Region[]>('api/region', { params });
  }

  getAPIKey(): Observable<any> {

    return this.http.get('api/region/getApiKey');
  }

  getOrganisationMarkers(uuid: string): Observable<Marker[]> {

    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Marker[]>('api/region/markers', { params });
  }

}
