import {User} from "eds-angular4/dist/security/models/User";

export class AuthorityToShare {
  organisationId: string;
  organisationName: string;
  organisationOdsCode: string;
  users: User[];
}
