import {User} from "./User";

export class AuthorityToShare {
  organisationId: string;
  organisationName: string;
  organisationOdsCode: string;
  users: User[];

  getDisplayItems() :any[] {
    return [
      {label: 'Organisation', property: 'organisationName'},
      {label: 'Users', property: 'users'},
    ];
  }
}
