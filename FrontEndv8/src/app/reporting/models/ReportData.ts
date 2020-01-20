export class ReportData {
  practiceName: string;
  odsCode: string;
  ccg: string;
  referenceAgreement: string;
  lastReceived: string;
  inError: string;

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'practiceName'},
      {label: 'ODS code', property: 'odsCode'},
      {label: 'CCG', property: 'ccg'},
      {label: 'Agreement', property: 'referenceAgreement'},
      {label: 'Last received', property: 'lastReceived'},
      {label: 'In error', property: 'inError'}
    ];
  }
}
