import {ValueSetCodes} from './ValueSetCodes';

export class ValueSets {

  uuid: string;
  name: string;
  valueSetsCodes: ValueSetCodes[];
  read2ConceptIds: string;
  ctv3ConceptIds: string;
  sctConceptIds: string;

  getDisplayItems(): any[] {
    return [
      {label: 'Name', property: 'name'},
      {label: 'Read2', property: 'read2ConceptIds'},
      {label: 'CTV3', property: 'ctv3ConceptIds'},
      {label: 'SNOMED', property: 'sctConceptIds'}
    ];
  }

}
