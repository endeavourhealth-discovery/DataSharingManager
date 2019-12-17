import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Schedule} from "./models/Schedule";

@Injectable()
export class SchedulerService {

  constructor(private http: HttpClient) {  }

  cronDescription(schedule?: Schedule): Observable<any> {
    return this.http.post<Schedule>('api/scheduler/description', schedule);
  }
}
