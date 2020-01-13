import { Injectable } from '@angular/core';
import {Project} from "./models/Project";
import {Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import { Dsa } from "src/app/data-sharing-agreement/models/Dsa";
import {Organisation} from "../organisation/models/Organisation";
import {Cohort} from "../cohort/models/Cohort";

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private http: HttpClient) { }

  getAllProjects(userId: string): Observable<Project[]> {
    const url = 'api/project';
    let params = new HttpParams();
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Project[]>(url,{params});
  }

  getProject(uuid: string): Observable<Project> {
    const url = 'api/project';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Project>(url,{params});
  }

  getLinkedDsas(uuid: string):  Observable<Dsa[]> {
    const url = 'api/project/dsas';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dsa[]>(url,{params});
  }

  getLinkedPublishers(uuid: string):  Observable<Organisation[]> {
    const url = 'api/project/publishers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>(url,{params});
  }

  getLinkedSubscribers(uuid: string):  Observable<Organisation[]> {
    const url = 'api/project/subscribers';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Organisation[]>(url,{params});
  }

  getLinkedBasePopulation(uuid: string):  Observable<Cohort[]> {
    const url = 'api/project/basePopulations';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Cohort[]>(url,{params});
  }

}
