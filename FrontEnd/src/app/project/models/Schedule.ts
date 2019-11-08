export class Schedule {
  projectUuid: string;
  starts: string;
  ends: string;
  frequency: string;
  weeks: string;
  isMonday: boolean;
  isTuesday: boolean;
  isWednesday: boolean;
  isThursday: boolean;
  isFriday: boolean;
  isSaturday: boolean;
  isSunday: boolean;

  constructor() {
    this.projectUuid = '';
    this.starts = '';
    this.ends = '';
    this.frequency = '';
    this.weeks = '';
    this.isMonday = false;
    this.isTuesday = false;
    this.isWednesday = false;
    this.isThursday = false;
    this.isFriday = false;
    this.isSaturday= false;
    this.isSunday= false;
  }
}
