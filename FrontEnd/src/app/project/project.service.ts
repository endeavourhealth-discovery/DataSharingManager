import { Injectable } from '@angular/core';
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {Organisation} from "../organisation/models/Organisation";
import {Dsa} from "../data-sharing-agreement/models/Dsa";
import {Project} from "./models/Project";
import {Cohort} from "../cohort/models/Cohort";
import {DataSet} from "../data-set/models/Dataset";

@Injectable()
export class ProjectService {

  constructor(private http: Http) { }

  getAllProjects(): Observable<Project[]> {
    const vm = this;
    return vm.http.get('api/project')
      .map((response) => response.json());
  }

  getProject(uuid: string): Observable<Project> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/project', { search : params })
      .map((response) => response.json());
  }

  saveProject(project: Project): Observable<any> {
    const vm = this;
    return vm.http.post('api/project', project)
      .map((response) => response.text());
  }

  deleteProject(uuid: string): Observable<any> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.delete('api/project', { search : params })
      .map((response) => response.text());
  }

  search(searchData: string): Observable<Project[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('searchData', searchData);
    return vm.http.get('api/project', { search : params })
      .map((response) => response.json());
  }

  getLinkedDsas(uuid: string):  Observable<Dsa[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/project/dsas', { search : params })
      .map((response) => response.json());
  }

  getLinkedPublishers(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/project/publishers', { search : params })
      .map((response) => response.json());
  }

  getLinkedSubscribers(uuid: string):  Observable<Organisation[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/project/subscribers', { search : params })
      .map((response) => response.json());
  }

  getLinkedBasePopulation(uuid: string):  Observable<Cohort[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/project/basePopulations', { search : params })
      .map((response) => response.json());
  }

  getLinkedDataSets(uuid: string):  Observable<DataSet[]> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('uuid', uuid);
    return vm.http.get('api/project/dataSets', { search : params })
      .map((response) => response.json());
  }

}