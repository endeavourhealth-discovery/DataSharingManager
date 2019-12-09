export class ConceptTreeNode {
  id: string;
  name: string;
  children: ConceptTreeNode[];
  level: number;
  expandable: boolean = true;
  isLoading: boolean;
}
