import {Organisation} from "./Organisation";

export class OrganisationPickerData {
  searchType: string;
  uuid: string;
  regionUUID: string;
  dsaUUID: string;
  existingOrgs: Organisation[];
}
