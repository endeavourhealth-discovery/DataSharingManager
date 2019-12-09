import {ConceptExpression} from './ConceptExpression';

export class ConceptDefinition {
  status: string;
  subtypeOf: ConceptExpression[];
  equivalentTo: ConceptExpression[];
  mappedTo: ConceptExpression[];
  termCodeOf: ConceptExpression;
  childOf: ConceptExpression;
  inversePropertyOf: ConceptExpression;
  disjointWith: ConceptExpression[];
}
