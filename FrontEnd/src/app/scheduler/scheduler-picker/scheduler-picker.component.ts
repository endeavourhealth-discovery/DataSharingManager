import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {SchedulerService} from '../scheduler.service';
import {LoggerService} from "eds-angular4";
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Schedule} from "../models/Schedule";

@Component({
  selector: 'app-scheduler-picker',
  templateUrl: './scheduler-picker.component.html',
  styleUrls: ['./scheduler-picker.component.css']
})
export class SchedulerPickerComponent implements OnInit, AfterViewInit {

  @Input() resultData: Schedule[];
  @ViewChild('tabset') tabset;

  cron: string;
  cronExpression: string;
  cronSettings: string;
  cronLabel: string;
  descriptionLabel: string;

  zeroTo59 = [
    {value: 0, display: '00'},
    {value: 1, display: '01'}, {value: 2, display: '02'}, {value: 3, display: '03'}, {value: 4, display: '04'}, {value: 5, display: '05'},
    {value: 6, display: '06'}, {value: 7, display: '07'}, {value: 8, display: '08'}, {value: 9, display: '09'}, {value: 10, display: '10'},
    {value: 11, display: '11'}, {value: 12, display: '12'}, {value: 13, display: '13'}, {value: 14, display: '14'}, {value: 15, display: '15'},
    {value: 16, display: '16'}, {value: 17, display: '17'}, {value: 18, display: '18'}, {value: 19, display: '19'}, {value: 20, display: '20'},
    {value: 21, display: '21'}, {value: 22, display: '22'}, {value: 23, display: '23'}, {value: 24, display: '24'}, {value: 25, display: '25'},
    {value: 26, display: '26'}, {value: 27, display: '27'}, {value: 28, display: '28'}, {value: 29, display: '29'}, {value: 30, display: '30'},
    {value: 31, display: '31'}, {value: 32, display: '32'}, {value: 33, display: '33'}, {value: 34, display: '34'}, {value: 35, display: '35'},
    {value: 36, display: '36'}, {value: 37, display: '37'}, {value: 38, display: '38'}, {value: 39, display: '39'}, {value: 40, display: '40'},
    {value: 41, display: '41'}, {value: 42, display: '42'}, {value: 43, display: '43'}, {value: 44, display: '44'}, {value: 45, display: '45'},
    {value: 46, display: '46'}, {value: 47, display: '47'}, {value: 48, display: '48'}, {value: 49, display: '49'}, {value: 50, display: '50'},
    {value: 51, display: '51'}, {value: 52, display: '52'}, {value: 53, display: '53'}, {value: 54, display: '54'}, {value: 55, display: '55'},
    {value: 56, display: '56'}, {value: 57, display: '57'}, {value: 58, display: '58'}, {value: 59, display: '59'}
  ];

  zeroTo23 = [
    {value: 0, display: '00'},
    {value: 1, display: '01'}, {value: 2, display: '02'}, {value: 3, display: '03'}, {value: 4, display: '04'}, {value: 5, display: '05'},
    {value: 6, display: '06'}, {value: 7, display: '07'}, {value: 8, display: '08'}, {value: 9, display: '09'}, {value: 10, display: '10'},
    {value: 11, display: '11'}, {value: 12, display: '12'}, {value: 13, display: '13'}, {value: 14, display: '14'}, {value: 15, display: '15'},
    {value: 16, display: '16'}, {value: 17, display: '17'}, {value: 18, display: '18'}, {value: 19, display: '19'}, {value: 20, display: '20'},
    {value: 21, display: '21'}, {value: 22, display: '22'}, {value: 23, display: '23'}
  ];

  oneTo31 = [
    {value: 1, display: '01'}, {value: 2, display: '02'}, {value: 3, display: '03'}, {value: 4, display: '04'}, {value: 5, display: '05'},
    {value: 6, display: '06'}, {value: 7, display: '07'}, {value: 8, display: '08'}, {value: 9, display: '09'}, {value: 10, display: '10'},
    {value: 11, display: '11'}, {value: 12, display: '12'}, {value: 13, display: '13'}, {value: 14, display: '14'}, {value: 15, display: '15'},
    {value: 16, display: '16'}, {value: 17, display: '17'}, {value: 18, display: '18'}, {value: 19, display: '19'}, {value: 20, display: '20'},
    {value: 21, display: '21'}, {value: 22, display: '22'}, {value: 23, display: '23'}, {value: 24, display: '24'}, {value: 25, display: '25'},
    {value: 26, display: '26'}, {value: 27, display: '27'}, {value: 28, display: '28'}, {value: 29, display: '29'}, {value: 30, display: '30'},
    {value: 31, display: '31'}
  ];

  days = [
    {value: '1W', display: 'First Weekday'}, {value: '1', display: '1st day'}, {value: '2', display: '2nd day'}, {value: '3', display: '3rd day'},
    {value: '4', display: '4th day'}, {value: '5', display: '5th day'}, {value: '6', display: '6th day'}, {value: '7', display: '7th day'},
    {value: '8', display: '8th day'}, {value: '9', display: '9th day'}, {value: '10', display: '10th day'}, {value: '11', display: '11th day'},
    {value: '12', display: '12th day'}, {value: '13', display: '13th day'}, {value: '14', display: '14th day'}, {value: '15', display: '15th day'},
    {value: '16', display: '16th day'}, {value: '17', display: '17th day'}, {value: '18', display: '18th day'}, {value: '19', display: '19th day'},
    {value: '20', display: '20th day'}, {value: '21', display: '21st day'}, {value: '22', display: '22nd day'}, {value: '23', display: '23rd day'},
    {value: '24', display: '24th day'}, {value: '25', display: '25th day'}, {value: '26', display: '26th day'}, {value: '27', display: '27th day'},
    {value: '28', display: '28th day'}, {value: '29', display: '29th day'}, {value: '30', display: '30th day'}, {value: '31', display: '31st day'},
    {value: 'LW', display: 'Last Weekday'}, {value: 'L', display: 'Last Day'}
  ];

  months = [
    {value: 1, display: '1'}, {value: 2, display: '2'}, {value: 3, display: '3'}, {value: 4, display: '4'}, {value: 5, display: '5'},
    {value: 6, display: '6'}, {value: 7, display: '7'}, {value: 8, display: '8'}, {value: 9, display: '9'}, {value: 10, display: '10'},
    {value: 11, display: '11'}, {value: 12, display: '12'}
  ];

  monthsLabelled = [
    {value: 1, display: 'January'}, {value: 2, display: 'February'}, {value: 3, display: 'March'}, {value: 4, display: 'April'},
    {value: 5, display: 'May'}, {value: 6, display: 'June'}, {value: 7, display: 'July'}, {value: 8, display: 'August'},
    {value: 9, display: 'September'}, {value: 10, display: 'October'}, {value: 11, display: 'November'}, {value: 12, display: 'December'}
  ];

  week = [
    {value: '#1', display: 'First'}, {value: '#2', display: 'Second'}, {value: '#3', display: 'Third'}, {value: '#4', display: 'Fourth'},
    {value: '#5', display: 'Fifth'}, {value: 'L', display: 'Last'}
  ];

  weekdays = [
    {value: 'MON', display: 'Monday'}, {value: 'TUE', display: 'Tuesday'}, {value: 'WED', display: 'Wednesday'}, {value: 'THU', display: 'Thursday'},
    {value: 'FRI', display: 'Friday'}, {value: 'SAT', display: 'Saturday'}, {value: 'SUN', display: 'Sunday'}
  ];

  //Minute Tab
  everyMinuteMinuteTab: number;
  everySecondMinuteTab: number;

  //Hourly Tab
  everyHourHourlyTab: number;
  everyMinuteHourlyTab: number;
  everySecondHourlyTab: number;

  //Daily Tab
  dailyRadio: boolean;
  dailyRow1: boolean;
  dailyRow2: boolean;
  everyDayDailyTab: number;
  everyHour1DailyTab: number;
  everyMinute1DailyTab: number;
  everySecond1DailyTab: number;
  everyHour2DailyTab: number;
  everyMinute2DailyTab: number;
  everySecond2DailyTab: number;

  //Weekly Tab
  monday: boolean;
  tuesday: boolean;
  wednesday: boolean;
  thursday: boolean;
  friday: boolean;
  saturday: boolean;
  sunday: boolean;
  everyHourWeeklyTab: number;
  everyMinuteWeeklyTab: number;
  everySecondWeeklyTab: number;

  //Monthly Tab
  monthlyRadio: boolean;
  monthlyRow1: boolean;
  monthlyRow2: boolean;
  everyDay1MonthlyTab: string;
  everyMonth1MonthlyTab: number;
  everyHour1MonthlyTab: number;
  everyMinute1MonthlyTab: number;
  everySecond1MonthlyTab: number;
  everyWeekMonthlyTab: string;
  everyDayMonthlyTab: string;
  everyMonthMonthlyTab: number;
  everyHour2MonthlyTab: number;
  everyMinute2MonthlyTab: number;
  everySecond2MonthlyTab: number;

  //Yearly Tab
  yearlyRadio: boolean;
  yearlyRow1: boolean;
  yearlyRow2: boolean;
  everyDay1YearlyTab: string;
  everyMonth1YearlyTab: number;
  everyHour1YearlyTab: number;
  everyMinute1YearlyTab: number;
  everySecond1YearlyTab: number;
  everyWeekYearlyTab: string;
  everyDayYearlyTab: string;
  everyMonthYearlyTab: number;
  everyHour2YearlyTab: number;
  everyMinute2YearlyTab: number;
  everySecond2YearlyTab: number;

  //Advanced Tab
  cronManual: string;

  constructor(public activeModal: NgbActiveModal,
              private log: LoggerService,
              protected schedulerService: SchedulerService) { }

  public static open(modalService: NgbModal, schedule: Schedule[]) {
    const modalRef = modalService.open(SchedulerPickerComponent, { backdrop : 'static'});
    modalRef.componentInstance.resultData = Object.assign([], schedule);
    return modalRef;
  }


  ngOnInit() {
    const vm = this;

    vm.cronLabel = "Cron Expression: ";
    vm.descriptionLabel = "Cron Description: ";
    vm.cronSettings = "";

    let schedule = vm.resultData[0];
    let tab = "";
    if (schedule) {
      vm.cron = vm.cronLabel + schedule.cronExpression;
      vm.cronExpression = vm.descriptionLabel + schedule.cronDescription;
      let split = schedule.cronSettings.split(":");
      tab = split[0];
    }


    if (tab != "Minutes") {
      vm.everyMinuteMinuteTab = 1;
      vm.everySecondMinuteTab = 0;
    }

    if (tab != "Hourly") {
      vm.everyHourHourlyTab = 1;
      vm.everyMinuteHourlyTab = 0;
      vm.everySecondHourlyTab = 0;
    }

    if (tab != "Daily") {
      vm.dailyRadio = true;
      vm.dailyRow1 = false;
      vm.dailyRow2 = true;
      vm.everyDayDailyTab = 1;
      vm.everyHour1DailyTab = 0;
      vm.everyMinute1DailyTab = 0;
      vm.everySecond1DailyTab = 0;
      vm.everyHour2DailyTab = 0;
      vm.everyMinute2DailyTab = 0;
      vm.everySecond2DailyTab = 0;
    }

    if (tab != "Weekly") {
      vm.everyHourWeeklyTab = 0;
      vm.everyMinuteWeeklyTab = 0;
      vm.everySecondWeeklyTab = 0;
      vm.monday = false;
      vm.tuesday = false;
      vm.wednesday = false;
      vm.thursday = false;
      vm.friday = false;
      vm.saturday = false;
      vm.sunday = false;
    }

    if (tab != "Monthly") {
      vm.monthlyRadio = true;
      vm.monthlyRow1 = false;
      vm.monthlyRow2 = true;
      vm.everyDay1MonthlyTab = "1";
      vm.everyMonth1MonthlyTab = 1;
      vm.everyHour1MonthlyTab = 0;
      vm.everyMinute1MonthlyTab = 0;
      vm.everySecond1MonthlyTab = 0;
      vm.everyWeekMonthlyTab = "#1";
      vm.everyDayMonthlyTab = "MON";
      vm.everyMonthMonthlyTab = 1;
      vm.everyHour2MonthlyTab = 0;
      vm.everyMinute2MonthlyTab = 0;
      vm.everySecond2MonthlyTab = 0;
    }

    if (tab != "Yearly") {
      vm.yearlyRadio = true;
      vm.yearlyRow1 = false;
      vm.yearlyRow2 = false;
      vm.everyDay1YearlyTab = "1";
      vm.everyMonth1YearlyTab = 1;
      vm.everyHour1YearlyTab = 0;
      vm.everyMinute1YearlyTab = 0;
      vm.everySecond1YearlyTab = 0;
      vm.everyWeekYearlyTab = "#1";
      vm.everyDayYearlyTab = "MON";
      vm.everyMonthYearlyTab = 1;
      vm.everyHour2YearlyTab = 0;
      vm.everyMinute2YearlyTab = 0;
      vm.everySecond2YearlyTab = 0;
    }

    if (tab != "Advanced") {
      vm.cronManual = "";
    }

    if (!vm.resultData[0]) {
      vm.setMinutesTabCron();
    }
  }

  ngAfterViewInit() {
    const vm = this;
    let schedule = vm.resultData[0];
    if (schedule) {
      let split = schedule.cronSettings.split(":");
      vm.tabset.select(split[0]);
      if (split[0] == "Minutes") {
        vm.everyMinuteMinuteTab = parseInt(split[2]);
        vm.everySecondMinuteTab = parseInt(split[1]);
        vm.setMinutesTabCron();

      } else if (split[0] == "Hourly") {
        vm.everyHourHourlyTab = parseInt(split[3]);
        vm.everyMinuteHourlyTab = parseInt(split[2]);
        vm.everySecondHourlyTab = parseInt(split[1]);
        vm.setHourlyTabCron();

      } else if (split[0] == "Daily") {
        if (split[1]=="true") {
          vm.dailyRadio = true;
          vm.everyDayDailyTab = parseInt(split[5]);
          vm.everyHour1DailyTab = parseInt(split[4]);
          vm.everyMinute1DailyTab = parseInt(split[3]);
          vm.everySecond1DailyTab = parseInt(split[2]);
          vm.everyHour2DailyTab = 0;
          vm.everyMinute2DailyTab = 0;
          vm.everySecond2DailyTab = 0;
        } else {
          vm.dailyRadio = false;
          vm.everyHour2DailyTab = parseInt(split[4]);
          vm.everyMinute2DailyTab = parseInt(split[3]);
          vm.everySecond2DailyTab = parseInt(split[2]);
          vm.everyDayDailyTab = 1;
          vm.everyHour1DailyTab = 0;
          vm.everyMinute1DailyTab = 0;
          vm.everySecond1DailyTab = 0;
        }
        vm.toggleDaily();

      } else if (split[0] == "Weekly") {
        vm.everyHourWeeklyTab = parseInt(split[3]);
        vm.everyMinuteWeeklyTab = parseInt(split[2]);
        vm.everySecondWeeklyTab = parseInt(split[1]);
        for (var i = 4; i < split.length; i++) {
          if (split[i] == "MON") {
            vm.monday = true;
          }
          if (split[i] == "TUE") {
            vm.tuesday = true;
          }
          if (split[i] == "WED") {
            vm.wednesday = true;
          }
          if (split[i] == "THU") {
            vm.thursday = true;
          }
          if (split[i] == "FRI") {
            vm.friday = true;
          }
          if (split[i] == "SAT") {
            vm.saturday = true;
          }
          if (split[i] == "SUN") {
            vm.sunday = true;
          }
        }
        vm.setWeeklyTabCron();

      } else if (split[0] == "Monthly") {
        if (split[1] == "true") {
          vm.monthlyRadio = true;
          vm.everyDay1MonthlyTab = split[5];
          vm.everyMonth1MonthlyTab = parseInt(split[6]);
          vm.everyHour1MonthlyTab = parseInt(split[4]);
          vm.everyMinute1MonthlyTab = parseInt(split[3]);
          vm.everySecond1MonthlyTab = parseInt(split[2]);
          vm.everyWeekMonthlyTab = "#1";
          vm.everyDayMonthlyTab = "MON";
          vm.everyMonthMonthlyTab = 1;
          vm.everyHour2MonthlyTab = 0;
          vm.everyMinute2MonthlyTab = 0;
          vm.everySecond2MonthlyTab = 0;
        } else {
          vm.monthlyRadio = false;
          vm.everyWeekMonthlyTab = split[7];
          vm.everyDayMonthlyTab = split[6];
          vm.everyMonthMonthlyTab = parseInt(split[5]);
          vm.everyHour2MonthlyTab = parseInt(split[4]);
          vm.everyMinute2MonthlyTab = parseInt(split[3]);
          vm.everySecond2MonthlyTab = parseInt(split[2]);
          vm.everyDay1MonthlyTab = "1";
          vm.everyMonth1MonthlyTab = 1;
          vm.everyHour1MonthlyTab = 0;
          vm.everyMinute1MonthlyTab = 0;
          vm.everySecond1MonthlyTab = 0;
        }
        vm.toggleMonthly();

      } else if (split[0] == "Yearly") {
        if (split[1] == "true") {
          vm.yearlyRadio = true;
          vm.everyDay1YearlyTab = split[5];
          vm.everyMonth1YearlyTab = parseInt(split[6])
          vm.everyHour1YearlyTab = parseInt(split[4]);
          vm.everyMinute1YearlyTab = parseInt(split[3]);
          vm.everySecond1YearlyTab = parseInt(split[2]);
          vm.everyWeekYearlyTab = "#1";
          vm.everyDayYearlyTab = "MON";
          vm.everyMonthYearlyTab = 1;
          vm.everyHour2YearlyTab = 0;
          vm.everyMinute2YearlyTab = 0;
          vm.everySecond2YearlyTab = 0;
        } else {
          vm.yearlyRadio = false;
          vm.everyWeekYearlyTab = split[7];
          vm.everyDayYearlyTab = split[6];
          vm.everyMonthYearlyTab = parseInt(split[5]);
          vm.everyHour2YearlyTab = parseInt(split[4]);
          vm.everyMinute2YearlyTab = parseInt(split[3]);
          vm.everySecond2YearlyTab = parseInt(split[2]);
          vm.everyDay1YearlyTab = "1";
          vm.everyMonth1YearlyTab = 1;
          vm.everyHour1YearlyTab = 0;
          vm.everyMinute1YearlyTab = 0;
          vm.everySecond1YearlyTab = 0;
        }
        vm.toggleYearly();

      } else if (split[0] == "Advanced") {
        vm.cronManual = schedule.cronExpression;
        vm.setAdvancedTabCron();

      } else {
        vm.setMinutesTabCron();
      }
    }
  }

  toggleDaily() {
    const vm = this;
    if (vm.dailyRadio) {
      vm.dailyRow1 = false;
      vm.dailyRow2 = true;
    } else {
      vm.dailyRow1 = true;
      vm.dailyRow2 = false;
    }
    vm.setDailyTabCron();
  }

  toggleMonthly() {
    const vm = this;
    if (vm.monthlyRadio) {
      vm.monthlyRow1 = false;
      vm.monthlyRow2 = true;
    } else {
      vm.monthlyRow1 = true;
      vm.monthlyRow2 = false;
    }
    vm.setMonthlyTabCron();
  }

  toggleYearly() {
    const vm = this;
    if (vm.yearlyRadio) {
      vm.yearlyRow1 = false;
      vm.yearlyRow2 = true;
    } else {
      vm.yearlyRow1 = true;
      vm.yearlyRow2 = false;
    }
    vm.setYearlyTabCron();
  }

  setMinutesTabCron() {
    const vm = this;
    vm.cron = vm.cronLabel + vm.everySecondMinuteTab + " 0/" + vm.everyMinuteMinuteTab + " * 1/1 * ? *";
    vm.cronSettings = "Minutes:" + vm.everySecondMinuteTab + ":" + vm.everyMinuteMinuteTab;
    vm.validateCron();
  }


  setHourlyTabCron() {
    const vm = this;
    vm.cron = vm.cronLabel + vm.everySecondHourlyTab + " " + vm.everyMinuteHourlyTab + " 0/" + vm.everyHourHourlyTab + " 1/1 * ? *";
    vm.cronSettings = "Hourly:" + vm.everySecondHourlyTab  + ":" + vm.everyMinuteHourlyTab + ":" + vm.everyHourHourlyTab;
    vm.validateCron();
  }

  setDailyTabCron() {
    const vm = this;
    if (vm.dailyRadio) {
      vm.cron = vm.cronLabel + vm.everySecond1DailyTab + " " + vm.everyMinute1DailyTab + " " + vm.everyHour1DailyTab + " 1/" + vm.everyDayDailyTab + " * ? *";
      vm.cronSettings = "Daily:true:" + vm.everySecond1DailyTab + ":" + vm.everyMinute1DailyTab + ":" + vm.everyHour1DailyTab + ":" + vm.everyDayDailyTab;
    } else {
      vm.cron = vm.cronLabel + vm.everySecond2DailyTab + " " + vm.everyMinute2DailyTab + " " + vm.everyHour2DailyTab + " ? * MON-FRI *";
      vm.cronSettings = "Daily:false:" + vm.everySecond2DailyTab + ":" + vm.everyMinute2DailyTab + ":" + vm.everyHour2DailyTab;
    }
    vm.validateCron();
  }

  setWeeklyTabCron() {
    const vm = this;
    vm.cron = vm.cronLabel + vm.everySecondWeeklyTab + " " + vm.everyMinuteWeeklyTab + " " + vm.everyHourWeeklyTab + " ? *";
    vm.cronSettings = "Weekly:" + vm.everySecondWeeklyTab + ":" + vm.everyMinuteWeeklyTab + ":" + vm.everyHourWeeklyTab;
    let prevDay = false;

    if (vm.monday) {
      vm.cron += " MON";
      vm.cronSettings += ":" + "MON";
      prevDay = true;
    }

    if (vm.tuesday) {
      vm.cronSettings += ":" + "TUE";
      if (prevDay) {
        vm.cron += ",TUE";
      } else {
        vm.cron += " TUE";
      }
      prevDay = true;
    }

    if (vm.wednesday) {
      vm.cronSettings += ":" + "WED";
      if (prevDay) {
        vm.cron += ",WED";
      } else {
        vm.cron += " WED";
      }
      prevDay = true;
    }

    if (vm.thursday) {
      vm.cronSettings += ":" + "THU";
      if (prevDay) {
        vm.cron += ",THU";
      } else {
        vm.cron += " THU";
      }
      prevDay = true;
    }

    if (vm.friday) {
      vm.cronSettings += ":" + "FRI";
      if (prevDay) {
        vm.cron += ",FRI";
      } else {
        vm.cron += " FRI";
      }
      prevDay = true;
    }

    if (vm.saturday) {
      vm.cronSettings += ":" + "SAT";
      if (prevDay) {
        vm.cron += ",SAT";
      } else {
        vm.cron += " SAT";
      }
      prevDay = true;
    }

    if (vm.sunday) {
      vm.cronSettings += ":" + "SUN";
      if (prevDay) {
        vm.cron += ",SUN";
      } else {
        vm.cron += " SUN";
      }
    }

    vm.cron += " *";
    vm.validateCron();
  }

  setMonthlyTabCron() {
    const vm = this;
    if (vm.monthlyRadio) {
      vm.cron = vm.cronLabel + vm.everySecond1MonthlyTab + " " + vm.everyMinute1MonthlyTab + " " + vm.everyHour1MonthlyTab + " " + vm.everyDay1MonthlyTab +" 1/" + vm.everyMonth1MonthlyTab +" ? *";
      vm.cronSettings = "Monthly:true:" + vm.everySecond1MonthlyTab + ":" + vm.everyMinute1MonthlyTab + ":" + vm.everyHour1MonthlyTab + ":" + vm.everyDay1MonthlyTab + ":" + vm.everyMonth1MonthlyTab;
    } else {
      vm.cron = vm.cronLabel + vm.everySecond2MonthlyTab + " " + vm.everyMinute2MonthlyTab + " " + vm.everyHour2MonthlyTab + " ? 1/" + vm.everyMonthMonthlyTab + " " + vm.everyDayMonthlyTab + vm.everyWeekMonthlyTab + " *";
      vm.cronSettings = "Monthly:false:" + vm.everySecond2MonthlyTab + ":" + vm.everyMinute2MonthlyTab + ":" + vm.everyHour2MonthlyTab + ":" + vm.everyMonthMonthlyTab + ":" + vm.everyDayMonthlyTab + ":" + vm.everyWeekMonthlyTab;
    }
    vm.validateCron();
  }

  setYearlyTabCron() {
    const vm = this;
    if (vm.yearlyRadio) {
      vm.cron =  vm.cronLabel + vm.everySecond1YearlyTab + " " + vm.everyMinute1YearlyTab + " " + vm.everyHour1YearlyTab + " " + vm.everyDay1YearlyTab + " " + vm.everyMonth1YearlyTab + " ? *";
      vm.cronSettings = "Yearly:true:" + vm.everySecond1YearlyTab + ":" + vm.everyMinute1YearlyTab + ":" + vm.everyHour1YearlyTab + ":" + vm.everyDay1YearlyTab + ":" + vm.everyMonth1YearlyTab;
    } else {
      vm.cron = vm.cronLabel + vm.everySecond2YearlyTab + " " + vm.everyMinute2YearlyTab + " " + vm.everyHour2YearlyTab + " ? " + vm.everyMonthYearlyTab + " " + vm.everyDayYearlyTab + vm.everyWeekYearlyTab + " *";
      vm.cronSettings = "Yearly:false:" + vm.everySecond2YearlyTab + ":" + vm.everyMinute2YearlyTab + ":" + vm.everyHour2YearlyTab + ":" + vm.everyMonthYearlyTab + ":" + vm.everyDayYearlyTab + ":" + vm.everyWeekYearlyTab;
    }
    vm.validateCron();
  }

  setAdvancedTabCron() {
    const vm = this;
    vm.cron = "";
    vm.cronExpression = "";
    if (vm.cronManual != "") {
      if (vm.cronManual != vm.cron) {
        vm.cron = vm.cronLabel + vm.cronManual;
        vm.cronSettings = "Advanced";
        vm.validateCron();
      }
    }
  }

  validateCron() {
    const vm = this;
    vm.schedulerService.cronDescription(vm.cron.substring(17, vm.cron.length))
      .subscribe(
        (response) => {
          vm.cronExpression = vm.descriptionLabel + response;
        },
        (error) => vm.log.error('Cron expression is invalid.', error, 'Describe cron expression.')
      );
  }

  ok() {
    const vm = this;
    if (!vm.resultData[0]) {
      let schedule = new Schedule();
      vm.resultData.push(schedule);
    }
    vm.resultData[0].cronExpression = vm.cron.substring(17, vm.cron.length);
    vm.resultData[0].cronDescription = vm.cronExpression.substring(18, vm.cronExpression.length);
    vm.resultData[0].cronSettings = vm.cronSettings;
    vm.activeModal.close(vm.resultData);
  }

  cancel() {
    this.activeModal.dismiss('cancel');
  }
}
