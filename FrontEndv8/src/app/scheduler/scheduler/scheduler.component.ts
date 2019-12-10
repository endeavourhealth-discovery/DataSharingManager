import {AfterViewInit, Component, Inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {LoggerService} from 'dds-angular8';
import {SchedulerService} from "../scheduler.service";
import {Schedule} from "../models/Schedule";

@Component({
  selector: 'app-scheduler',
  templateUrl: './scheduler.component.html',
  styleUrls: ['./scheduler.component.css']
})
export class SchedulerComponent implements AfterViewInit {

  schedule: Schedule;
  cron: string;
  cronDescription: string;
  cronSettings: string;

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

  rows = ["1","2"];

  //Minute Tab
  everyMinuteMinuteTab: number;
  everySecondMinuteTab: number;

  //Hourly Tab
  everyHourHourlyTab: number;
  everyMinuteHourlyTab: number;
  everySecondHourlyTab: number;

  //Daily Tab
  dailyRadio: string;
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

  constructor(private schedulerService: SchedulerService,
              private log: LoggerService) { }

  ngAfterViewInit() {

    let tab = "";
    if (this.schedule) {
      this.cron = this.schedule.cronExpression;
      this.cronDescription = this.schedule.cronDescription;
      //let split = this.schedule.cronSettings.split(":");
      //tab = split[0];
    }

    if (tab != "Minutes") {
      this.everyMinuteMinuteTab = 1;
      this.everySecondMinuteTab = 0;
    }

    if (tab != "Hourly") {
      this.everyHourHourlyTab = 1;
      this.everyMinuteHourlyTab = 0;
      this.everySecondHourlyTab = 0;
    }

    if (tab != "Daily") {
      this.dailyRadio = "1";
      this.dailyRow1 = false;
      this.dailyRow2 = true;
      this.everyDayDailyTab = 1;
      this.everyHour1DailyTab = 0;
      this.everyMinute1DailyTab = 0;
      this.everySecond1DailyTab = 0;
      this.everyHour2DailyTab = 0;
      this.everyMinute2DailyTab = 0;
      this.everySecond2DailyTab = 0;
    }

    if (tab != "Weekly") {
      this.everyHourWeeklyTab = 0;
      this.everyMinuteWeeklyTab = 0;
      this.everySecondWeeklyTab = 0;
      this.monday = false;
      this.tuesday = false;
      this.wednesday = false;
      this.thursday = false;
      this.friday = false;
      this.saturday = false;
      this.sunday = false;
    }

    if (tab != "Monthly") {
      this.monthlyRadio = true;
      this.monthlyRow1 = false;
      this.monthlyRow2 = true;
      this.everyDay1MonthlyTab = "1";
      this.everyMonth1MonthlyTab = 1;
      this.everyHour1MonthlyTab = 0;
      this.everyMinute1MonthlyTab = 0;
      this.everySecond1MonthlyTab = 0;
      this.everyWeekMonthlyTab = "#1";
      this.everyDayMonthlyTab = "MON";
      this.everyMonthMonthlyTab = 1;
      this.everyHour2MonthlyTab = 0;
      this.everyMinute2MonthlyTab = 0;
      this.everySecond2MonthlyTab = 0;
    }

    if (tab != "Yearly") {
      this.yearlyRadio = true;
      this.yearlyRow1 = false;
      this.yearlyRow2 = false;
      this.everyDay1YearlyTab = "1";
      this.everyMonth1YearlyTab = 1;
      this.everyHour1YearlyTab = 0;
      this.everyMinute1YearlyTab = 0;
      this.everySecond1YearlyTab = 0;
      this.everyWeekYearlyTab = "#1";
      this.everyDayYearlyTab = "MON";
      this.everyMonthYearlyTab = 1;
      this.everyHour2YearlyTab = 0;
      this.everyMinute2YearlyTab = 0;
      this.everySecond2YearlyTab = 0;
    }

    if (tab != "Advanced") {
      this.cronManual = "";
    }

    this.setMinutesTabCron();
  }

  setTab($event){
    if ($event.index == 0) {
      this.setMinutesTabCron();
    } else if ($event.index == 1) {
      this.setHourlyTabCron();
    } else if ($event.index == 2) {
      this.setDailyTabCron();
    }
  }

  setMinutesTabCron() {
    this.cron = this.everySecondMinuteTab + " 0/" + this.everyMinuteMinuteTab + " * 1/1 * ? *";
    this.cronSettings = "Minutes:" + this.everySecondMinuteTab + ":" + this.everyMinuteMinuteTab;
    this.validateCron();
  }

  setHourlyTabCron() {
    this.cron = this.everySecondHourlyTab + " " + this.everyMinuteHourlyTab + " 0/" + this.everyHourHourlyTab + " 1/1 * ? *";
    this.cronSettings = "Hourly:" + this.everySecondHourlyTab  + ":" + this.everyMinuteHourlyTab + ":" + this.everyHourHourlyTab;
    this.validateCron();
  }

  setDailyTabCron() {
    if (this.dailyRadio == "1") {
      this.cron = this.everySecond1DailyTab + " " + this.everyMinute1DailyTab + " " + this.everyHour1DailyTab + " 1/" + this.everyDayDailyTab + " * ? *";
      this.cronSettings = "Daily:1:" + this.everySecond1DailyTab + ":" + this.everyMinute1DailyTab + ":" + this.everyHour1DailyTab + ":" + this.everyDayDailyTab;
    } else {
      this.cron = this.everySecond2DailyTab + " " + this.everyMinute2DailyTab + " " + this.everyHour2DailyTab + " ? * MON-FRI *";
      this.cronSettings = "Daily:2:" + this.everySecond2DailyTab + ":" + this.everyMinute2DailyTab + ":" + this.everyHour2DailyTab;
    }
    this.validateCron();
  }

  toggleDaily() {
    if (this.dailyRadio == "1") {
      this.dailyRow1 = false;
      this.dailyRow2 = true;
    } else {
      this.dailyRow1 = true;
      this.dailyRow2 = false;
    }
    this.setDailyTabCron();
  }

  validateCron() {
    if (!this.schedule) {
      this.schedule = new Schedule();
      this.schedule.uuid = "";
      this.schedule.cronExpression = this.cron;
      this.schedule.cronSettings = this.cronSettings;
    } else {
      this.schedule.cronExpression = this.cron;
      this.schedule.cronSettings = this.cronSettings;
    }
    this.schedulerService.cronDescription(this.schedule)
      .subscribe(
        (result) => {
          this.schedule = result;
          this.cronDescription = this.schedule.cronDescription;
          console.log(this.schedule);
        },
        (error) => { this.log.error(error) }
      );
  }
}
