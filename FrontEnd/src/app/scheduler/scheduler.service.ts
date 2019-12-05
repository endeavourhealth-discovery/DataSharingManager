import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Http} from "@angular/http";
import {Cron} from "./models/Cron";
import {Schedule} from "./models/Schedule";

@Injectable()
export class SchedulerService {

  cronObject: Cron;

  constructor(private http: Http) { }

  cronDescription(schedule: Schedule): Observable<Schedule> {
    const vm = this;
    return vm.http.post('api/scheduler/description', schedule)
      .map((response) => response.json());
  }
}
