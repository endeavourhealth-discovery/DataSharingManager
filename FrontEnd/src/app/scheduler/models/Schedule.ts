export class Schedule {

  uuid: string;
  cronExpression: string;
  cronDescription: string;
  schedulerSettings: string;

  constructor() {
    this.uuid = '';
    this.cronExpression = '';
    this.cronDescription = '';
    this.schedulerSettings = '';
  }
}
