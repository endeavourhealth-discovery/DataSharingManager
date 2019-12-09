import { Injectable } from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Instance} from '../models/Instance';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class InstanceService {

  constructor(private http: HttpClient) { }

  getInstances(): Observable<Instance[]> {
    return this.http.get<Instance[]>('api/instances');
  }
}
