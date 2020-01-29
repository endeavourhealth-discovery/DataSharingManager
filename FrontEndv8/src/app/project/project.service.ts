import {Injectable} from '@angular/core';
import {Project} from "./models/Project";
import {Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Dsa} from "src/app/data-sharing-agreement/models/Dsa";
import {Organisation} from "../organisation/models/Organisation";
import {Cohort} from "../cohort/models/Cohort";
import {DataSet} from "../data-set/models/Dataset";
import {AuthorityToShare} from "src/app/project/models/AuthorityToShare";
import {ProjectApplicationPolicy} from "./models/ProjectApplicationPolicy";
import {ApplicationPolicy} from "./models/ApplicationPolicy";
import {User} from './models/User';
import {Schedule} from "../scheduler/models/Schedule";
import {ExtractTechnicalDetails} from "./models/ExtractTechnicalDetails";

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

  search(searchData: string): Observable<Project[]> {
    const url = 'api/project';
    let params = new HttpParams();
    if (searchData) params = params.append('searchData', searchData);
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

  getLinkedDataSets(uuid: string):  Observable<DataSet[]> {
    const url = 'api/project/dataSets';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<DataSet[]>(url,{params});
  }

  getUsersAssignedToProject(uuid: string):  Observable<AuthorityToShare[]> {
    const url = 'api/project/getUsersAssignedToProject';
    let params = new HttpParams();
    if (uuid) params = params.append('projectUuid', uuid);
    return this.http.get<AuthorityToShare[]>(url,{params});
  }

  getAvailableProjectApplicationPolicy(): Observable<ApplicationPolicy[]> {
    const url = 'api/project/getApplicationPolicies';
    return this.http.get<ApplicationPolicy[]>(url);
  }

  getProjectApplicationPolicy(uuid: string): Observable<ProjectApplicationPolicy> {
    const url = 'api/project/projectApplicationPolicy';
    let params = new HttpParams();
    if (uuid) params = params.append('projectUuid', uuid);
    return this.http.get<ProjectApplicationPolicy>(url,{params});
  }

  getUsers(): Observable<User[]> {
    const url = 'api/project/getUsers';
    return this.http.get<User[]>(url);
  }

  getLinkedSchedule(uuid: string):  Observable<Schedule> {
    const url = 'api/project/schedule';
    let params = new HttpParams();
    if (uuid) params = params.append('uuid', uuid);
    return this.http.get<Schedule>(url,{params});
  }

  getAssociatedExtractTechDetails(uuid: string): Observable<ExtractTechnicalDetails> {
    const url = 'api/extractTechnicalDetails/associated';
    let params = new HttpParams();
    if (uuid) {
      params = params.append('parentUuid', uuid);
      params = params.append('parentType', '14');
    }
    return this.http.get<ExtractTechnicalDetails>(url,{params});
  }

  saveProject(project: Project): Observable<any> {
    const url = 'api/project';
    return this.http.post(url, project, {responseType: 'text'});
  }

  updateMappings(project: Project): Observable<any> {
    const url = 'api/project/updateMappings';
    return this.http.post(url, project, {responseType: 'text'});
  }

  saveProjectApplicationPolicy(projectApplicationPolicy: ProjectApplicationPolicy): Observable<string> {
    const url = 'api/project/setProjectApplicationPolicy';
    return this.http.post(url, projectApplicationPolicy, {responseType: 'text'});
  }

  deleteProject(uuid: string[]): Observable<any> {
    const url = 'api/project';
    let params = new HttpParams();
    for (let ix in uuid) {
      params = params.append('uuids', uuid[ix]);
    }
    return this.http.delete(url,{params});
  }
}
