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

  getDisplayItems(): any[] {
    return [
      {label: 'Expression', property: 'cronExpression', secondary: false},
      {label: 'Description', property: 'cronDescription', secondary: false}
    ];
  }
}
