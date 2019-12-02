import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Http} from "@angular/http";
import {Cron} from "./models/Cron";

@Injectable()
export class SchedulerService {

  cronObject: Cron;

  constructor(private http: Http) { }

  cronDescription(cron: string): Observable<string> {
    const vm = this;
    vm.cronObject = new Cron();
    vm.cronObject.cron = cron;
    return vm.http.post('api/scheduler/description', vm.cronObject)
      .map((response) => response.text());
  }
}
