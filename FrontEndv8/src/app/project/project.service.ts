import { Injectable } from '@angular/core';
import {Project} from "./models/Project";
import {Observable} from "rxjs";
import * as Http from "http";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Dpa} from "../data-processing-agreement/models/Dpa";

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private http: HttpClient) { }

  getAllProjects(userId: string): Observable<Dpa[]> {
    const url = 'api/project';
    let params = new HttpParams();
    if (userId != null) {
      if (userId) params = params.append('userId', userId);
    }
    return this.http.get<Dpa[]>(url,{params});
  }

  getProject(uuid: string): Observable<Dpa> {
    const url = 'api/project';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Dpa>(url,{params});
  }

}
