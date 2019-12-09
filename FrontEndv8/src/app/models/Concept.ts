import {ConceptSummary} from './ConceptSummary';
import {ConceptDefinition} from './ConceptDefinition';

export class Concept {
  model: string;
  concept: ConceptSummary;
  definition: ConceptDefinition;
  propertyRange: any;

  // DEPRECATED
  revision: string;
  published: string;
}
