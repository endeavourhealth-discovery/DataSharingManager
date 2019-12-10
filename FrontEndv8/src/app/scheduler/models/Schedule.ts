export class Schedule {

  uuid: string;
  cronExpression: string;
  cronDescription: string;
  cronSettings: string;

  constructor() {
    this.uuid = '';
    this.cronExpression = '';
    this.cronDescription = '';
    this.cronSettings = '';
  }
}
