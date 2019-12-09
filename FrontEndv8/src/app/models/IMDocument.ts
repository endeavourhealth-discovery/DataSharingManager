import {Version} from './Version';

export class IMDocument {
  dbid : number;
  path: string;
  version: Version;
  drafts: number;

  instanceVersion: Version;
  instanceDrafts: number;
}
