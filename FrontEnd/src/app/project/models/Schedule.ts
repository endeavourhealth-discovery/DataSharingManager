export class Schedule {
  uuid: string;
  starts: string;
  ends: string;
  frequency: number;
  weeks: string;
  monday: boolean;
  tuesday: boolean;
  wednesday: boolean;
  thursday: boolean;
  friday: boolean;
  saturday: boolean;
  sunday: boolean;

  constructor() {
    this.uuid = '';
    this.starts = '';
    this.ends = '';
    this.frequency = 0;
    this.weeks = '';
    this.monday = false;
    this.tuesday = false;
    this.wednesday = false;
    this.thursday = false;
    this.friday = false;
    this.saturday= false;
    this.sunday= false;
  }
}
