import {Component, Inject, OnInit} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {LoggerService} from 'dds-angular8';
import {SchedulerService} from "../scheduler.service";
import {Schedule} from "../models/Schedule";

export interface DialogData {
  schedule: Schedule;
  allowTime: boolean;
}

@Component({
  selector: 'app-scheduler',
  templateUrl: './scheduler.component.html',
  styleUrls: ['./scheduler.component.css']
})
export class SchedulerComponent implements OnInit {

  allowTime: boolean;
  schedule: Schedule;
  cron: string;
  cronDescription: string;
  cronSettings: string;
  formIsInvalid: boolean;

  zeroTo59 = [
    {value: '0', display: '00'},
    {value: '1', display: '01'}, {value: '2', display: '02'}, {value: '3', display: '03'}, {value: '4', display: '04'}, {value: '5', display: '05'},
    {value: '6', display: '06'}, {value: '7', display: '07'}, {value: '8', display: '08'}, {value: '9', display: '09'}, {value: '10', display: '10'},
    {value: '11', display: '11'}, {value: '12', display: '12'}, {value: '13', display: '13'}, {value: '14', display: '14'}, {value: '15', display: '15'},
    {value: '16', display: '16'}, {value: '17', display: '17'}, {value: '18', display: '18'}, {value: '19', display: '19'}, {value: '20', display: '20'},
    {value: '21', display: '21'}, {value: '22', display: '22'}, {value: '23', display: '23'}, {value: '24', display: '24'}, {value: '25', display: '25'},
    {value: '26', display: '26'}, {value: '27', display: '27'}, {value: '28', display: '28'}, {value: '29', display: '29'}, {value: '30', display: '30'},
    {value: '31', display: '31'}, {value: '32', display: '32'}, {value: '33', display: '33'}, {value: '34', display: '34'}, {value: '35', display: '35'},
    {value: '36', display: '36'}, {value: '37', display: '37'}, {value: '38', display: '38'}, {value: '39', display: '39'}, {value: '40', display: '40'},
    {value: '41', display: '41'}, {value: '42', display: '42'}, {value: '43', display: '43'}, {value: '44', display: '44'}, {value: '45', display: '45'},
    {value: '46', display: '46'}, {value: '47', display: '47'}, {value: '48', display: '48'}, {value: '49', display: '49'}, {value: '50', display: '50'},
    {value: '51', display: '51'}, {value: '52', display: '52'}, {value: '53', display: '53'}, {value: '54', display: '54'}, {value: '55', display: '55'},
    {value: '56', display: '56'}, {value: '57', display: '57'}, {value: '58', display: '58'}, {value: '59', display: '59'}
  ];

  zeroTo23 = [
    {value: '0', display: '00'},
    {value: '1', display: '01'}, {value: '2', display: '02'}, {value: '3', display: '03'}, {value: '4', display: '04'}, {value: '5', display: '05'},
    {value: '6', display: '06'}, {value: '7', display: '07'}, {value: '8', display: '08'}, {value: '9', display: '09'}, {value: '10', display: '10'},
    {value: '11', display: '11'}, {value: '12', display: '12'}, {value: '13', display: '13'}, {value: '14', display: '14'}, {value: '15', display: '15'},
    {value: '16', display: '16'}, {value: '17', display: '17'}, {value: '18', display: '18'}, {value: '19', display: '19'}, {value: '20', display: '20'},
    {value: '21', display: '21'}, {value: '22', display: '22'}, {value: '23', display: '23'}
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
    {value: '1', display: '1'}, {value: '2', display: '2'}, {value: '3', display: '3'}, {value: '4', display: '4'}, {value: '5', display: '5'},
    {value: '6', display: '6'}, {value: '7', display: '7'}, {value: '8', display: '8'}, {value: '9', display: '9'}, {value: '10', display: '10'},
    {value: '11', display: '11'}, {value: '12', display: '12'}
  ];

  monthsLabelled = [
    {value: '1', display: 'January'}, {value: '2', display: 'February'}, {value: '3', display: 'March'}, {value: '4', display: 'April'},
    {value: '5', display: 'May'}, {value: '6', display: 'June'}, {value: '7', display: 'July'}, {value: '8', display: 'August'},
    {value: '9', display: 'September'}, {value: '10', display: 'October'}, {value: '11', display: 'November'}, {value: '12', display: 'December'}
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

  selectedTab: number;

  //Minute Tab
  everyMinuteMinuteTab: string;
  everySecondMinuteTab: string;

  //Hourly Tab
  everyHourHourlyTab: string;
  everyMinuteHourlyTab: string;
  everySecondHourlyTab: string;

  //Daily Tab
  dailyRadio: string;
  dailyRow1: boolean;
  dailyRow2: boolean;
  everyDayDailyTab: string;
  everyHour1DailyTab: string;
  everyMinute1DailyTab: string;
  everySecond1DailyTab: string;
  everyHour2DailyTab: string;
  everyMinute2DailyTab: string;
  everySecond2DailyTab: string;

  //Weekly Tab
  monday: boolean;
  tuesday: boolean;
  wednesday: boolean;
  thursday: boolean;
  friday: boolean;
  saturday: boolean;
  sunday: boolean;
  everyHourWeeklyTab: string;
  everyMinuteWeeklyTab: string;
  everySecondWeeklyTab: string;

  //Monthly Tab
  monthlyRadio: string;
  monthlyRow1: boolean;
  monthlyRow2: boolean;
  everyDay1MonthlyTab: string;
  everyMonth1MonthlyTab: string;
  everyHour1MonthlyTab: string;
  everyMinute1MonthlyTab: string;
  everySecond1MonthlyTab: string;
  everyWeekMonthlyTab: string;
  everyDayMonthlyTab: string;
  everyMonthMonthlyTab: string;
  everyHour2MonthlyTab: string;
  everyMinute2MonthlyTab: string;
  everySecond2MonthlyTab: string;

  //Yearly Tab
  yearlyRadio: string;
  yearlyRow1: boolean;
  yearlyRow2: boolean;
  everyDay1YearlyTab: string;
  everyMonth1YearlyTab: string;
  everyHour1YearlyTab: string;
  everyMinute1YearlyTab: string;
  everySecond1YearlyTab: string;
  everyWeekYearlyTab: string;
  everyDayYearlyTab: string;
  everyMonthYearlyTab: string;
  everyHour2YearlyTab: string;
  everyMinute2YearlyTab: string;
  everySecond2YearlyTab: string;

  //Advanced Tab
  cronManual: string;

  constructor(public dialogRef: MatDialogRef<SchedulerComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private schedulerService: SchedulerService,
              private log: LoggerService) {

    this.schedule = data.schedule;
    this.allowTime = data.allowTime;
  }

  ngOnInit() {

    this.formIsInvalid = false;
    let tab = "";
    this.selectedTab = 0;
    if (this.schedule) {
      this.cron = this.schedule.cronExpression;
      this.cronDescription = this.schedule.cronDescription;
      let split = this.schedule.cronSettings.split(":");
      this.populateFields(split);
      tab = split[0];
    }

    if (tab != "Minutes") {
      this.everyMinuteMinuteTab = "1";
      this.everySecondMinuteTab = "0";
    }

    if (tab != "Hourly") {
      this.everyHourHourlyTab = "1";
      this.everyMinuteHourlyTab = "0";
      this.everySecondHourlyTab = "0";
    }

    if (tab != "Daily") {
      this.dailyRadio = "1";
      this.dailyRow1 = false;
      this.dailyRow2 = true;
      this.everyDayDailyTab = "1";
      this.everyHour1DailyTab = "0";
      this.everyMinute1DailyTab = "0";
      this.everySecond1DailyTab = "0";
      this.everyHour2DailyTab = "0";
      this.everyMinute2DailyTab = "0";
      this.everySecond2DailyTab = "0";
    }

    if (tab != "Weekly") {
      this.everyHourWeeklyTab = "0";
      this.everyMinuteWeeklyTab = "0";
      this.everySecondWeeklyTab = "0";
      this.monday = false;
      this.tuesday = false;
      this.wednesday = false;
      this.thursday = false;
      this.friday = false;
      this.saturday = false;
      this.sunday = false;
    }

    if (tab != "Monthly") {
      this.monthlyRadio = "1";
      this.monthlyRow1 = false;
      this.monthlyRow2 = true;
      this.everyDay1MonthlyTab = "1";
      this.everyMonth1MonthlyTab = "1";
      this.everyHour1MonthlyTab = "0";
      this.everyMinute1MonthlyTab = "0";
      this.everySecond1MonthlyTab = "0";
      this.everyWeekMonthlyTab = "#1";
      this.everyDayMonthlyTab = "MON";
      this.everyMonthMonthlyTab = "1";
      this.everyHour2MonthlyTab = "0";
      this.everyMinute2MonthlyTab = "0";
      this.everySecond2MonthlyTab = "0";
    }

    if (tab != "Yearly") {
      this.yearlyRadio = "1";
      this.yearlyRow1 = false;
      this.yearlyRow2 = true;
      this.everyDay1YearlyTab = "1";
      this.everyMonth1YearlyTab = "1";
      this.everyHour1YearlyTab = "0";
      this.everyMinute1YearlyTab = "0";
      this.everySecond1YearlyTab = "0";
      this.everyWeekYearlyTab = "#1";
      this.everyDayYearlyTab = "MON";
      this.everyMonthYearlyTab = "1";
      this.everyHour2YearlyTab = "0";
      this.everyMinute2YearlyTab = "0";
      this.everySecond2YearlyTab = "0";
    }

    if (tab != "Advanced") {
      this.cronManual = "";
    }

    if (tab == "") {
      if (this.allowTime) {
        this.setMinutesTabCron();
      } else {
        this.setDailyTabCron();
      }
    }
  }

  populateFields(split: string[]) {
    if (split[0] == "Minutes") {
      this.everyMinuteMinuteTab = split[2];
      this.everySecondMinuteTab = split[1];
      this.setMinutesTabCron();
      this.selectedTab = 0;

    } else if (split[0] == "Hourly") {
      this.everyHourHourlyTab = split[3];
      this.everyMinuteHourlyTab = split[2];
      this.everySecondHourlyTab = split[1];
      this.setHourlyTabCron();
      this.selectedTab = 1;

    } else if (split[0] == "Daily") {
      if (split[1]=="1") {
        this.dailyRadio = "1";
        this.everyDayDailyTab = split[5];
        this.everyHour1DailyTab = split[4];
        this.everyMinute1DailyTab = split[3];
        this.everySecond1DailyTab = split[2];
        this.everyHour2DailyTab = "0";
        this.everyMinute2DailyTab = "0";
        this.everySecond2DailyTab = "0";
      } else {
        this.dailyRadio = "2";
        this.everyHour2DailyTab = split[4];
        this.everyMinute2DailyTab = split[3];
        this.everySecond2DailyTab = split[2];
        this.everyDayDailyTab = "1";
        this.everyHour1DailyTab = "0";
        this.everyMinute1DailyTab = "0";
        this.everySecond1DailyTab = "0";
      }
      this.toggleDaily();
      if (this.allowTime) {
        this.selectedTab = 2;
      } else {
        this.selectedTab = 0;
      }
    } else if (split[0] == "Weekly") {
      this.everyHourWeeklyTab = split[3];
      this.everyMinuteWeeklyTab = split[2];
      this.everySecondWeeklyTab = split[1];
      for (var i = 4; i < split.length; i++) {
        if (split[i] == "MON") {
          this.monday = true;
        }
        if (split[i] == "TUE") {
          this.tuesday = true;
        }
        if (split[i] == "WED") {
          this.wednesday = true;
        }
        if (split[i] == "THU") {
          this.thursday = true;
        }
        if (split[i] == "FRI") {
          this.friday = true;
        }
        if (split[i] == "SAT") {
          this.saturday = true;
        }
        if (split[i] == "SUN") {
          this.sunday = true;
        }
      }
      this.setWeeklyTabCron();
      if (this.allowTime) {
        this.selectedTab = 3;
      } else {
        this.selectedTab = 1;
      }

    } else if (split[0] == "Monthly") {
      if (split[1] == "1") {
        this.monthlyRadio = "1";
        this.everyDay1MonthlyTab = split[5];
        this.everyMonth1MonthlyTab = split[6];
        this.everyHour1MonthlyTab = split[4];
        this.everyMinute1MonthlyTab = split[3];
        this.everySecond1MonthlyTab = split[2];
        this.everyWeekMonthlyTab = "#1";
        this.everyDayMonthlyTab = "MON";
        this.everyMonthMonthlyTab = "1";
        this.everyHour2MonthlyTab = "0";
        this.everyMinute2MonthlyTab = "0";
        this.everySecond2MonthlyTab = "0";
      } else {
        this.monthlyRadio = "2";
        this.everyWeekMonthlyTab = split[7];
        this.everyDayMonthlyTab = split[6];
        this.everyMonthMonthlyTab = split[5];
        this.everyHour2MonthlyTab = split[4];
        this.everyMinute2MonthlyTab = split[3];
        this.everySecond2MonthlyTab = split[2];
        this.everyDay1MonthlyTab = "1";
        this.everyMonth1MonthlyTab = "1";
        this.everyHour1MonthlyTab = "0";
        this.everyMinute1MonthlyTab = "0";
        this.everySecond1MonthlyTab = "0";
      }
      this.toggleMonthly();
      if (this.allowTime) {
        this.selectedTab = 4;
      } else {
        this.selectedTab = 2;
      }

    } else if (split[0] == "Yearly") {
      if (split[1] == "1") {
        this.yearlyRadio = "1";
        this.everyDay1YearlyTab = split[5];
        this.everyMonth1YearlyTab = split[6]
        this.everyHour1YearlyTab = split[4];
        this.everyMinute1YearlyTab = split[3];
        this.everySecond1YearlyTab = split[2];
        this.everyWeekYearlyTab = "#1";
        this.everyDayYearlyTab = "MON";
        this.everyMonthYearlyTab = "1";
        this.everyHour2YearlyTab = "0";
        this.everyMinute2YearlyTab = "0";
        this.everySecond2YearlyTab = "0";
      } else {
        this.yearlyRadio = "2";
        this.everyWeekYearlyTab = split[7];
        this.everyDayYearlyTab = split[6];
        this.everyMonthYearlyTab = split[5];
        this.everyHour2YearlyTab = split[4];
        this.everyMinute2YearlyTab = split[3];
        this.everySecond2YearlyTab = split[2];
        this.everyDay1YearlyTab = "1";
        this.everyMonth1YearlyTab = "1";
        this.everyHour1YearlyTab = "0";
        this.everyMinute1YearlyTab = "0";
        this.everySecond1YearlyTab = "0";
      }
      this.toggleYearly();
      if (this.allowTime) {
        this.selectedTab = 5;
      } else {
        this.selectedTab = 3;
      }

    } else if (split[0] == "Advanced") {
      this.cronManual = this.schedule.cronExpression;
      this.setAdvancedTabCron();
      if (this.allowTime) {
        this.selectedTab = 6;
      } else {
        this.selectedTab = 4;
      }
    }
  }

  setTab($event){
    if (this.allowTime) {
      if ($event.index == 0) {
        this.setMinutesTabCron();
      } else if ($event.index == 1) {
        this.setHourlyTabCron();
      } else if ($event.index == 2) {
        this.setDailyTabCron();
      } else if ($event.index == 3) {
        this.setWeeklyTabCron();
      } else if ($event.index == 4) {
        this.setMonthlyTabCron();
      } else if ($event.index == 5) {
        this.setYearlyTabCron();
      } else if ($event.index == 6) {
        this.setAdvancedTabCron();
      }
    } else {
      if ($event.index == 0) {
        this.setDailyTabCron();
      } else if ($event.index == 1) {
        this.setWeeklyTabCron();
      } else if ($event.index == 2) {
        this.setMonthlyTabCron();
      } else if ($event.index == 3) {
        this.setYearlyTabCron();
      } else if ($event.index == 4) {
        this.setAdvancedTabCron();
      }
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

  setWeeklyTabCron() {
    this.cron = this.everySecondWeeklyTab + " " + this.everyMinuteWeeklyTab + " " + this.everyHourWeeklyTab + " ? *";
    this.cronSettings = "Weekly:" + this.everySecondWeeklyTab + ":" + this.everyMinuteWeeklyTab + ":" + this.everyHourWeeklyTab;
    let prevDay = false;

    if (this.monday) {
      this.cron += " MON";
      this.cronSettings += ":" + "MON";
      prevDay = true;
    }

    if (this.tuesday) {
      this.cronSettings += ":" + "TUE";
      if (prevDay) {
        this.cron += ",TUE";
      } else {
        this.cron += " TUE";
      }
      prevDay = true;
    }

    if (this.wednesday) {
      this.cronSettings += ":" + "WED";
      if (prevDay) {
        this.cron += ",WED";
      } else {
        this.cron += " WED";
      }
      prevDay = true;
    }

    if (this.thursday) {
      this.cronSettings += ":" + "THU";
      if (prevDay) {
        this.cron += ",THU";
      } else {
        this.cron += " THU";
      }
      prevDay = true;
    }

    if (this.friday) {
      this.cronSettings += ":" + "FRI";
      if (prevDay) {
        this.cron += ",FRI";
      } else {
        this.cron += " FRI";
      }
      prevDay = true;
    }

    if (this.saturday) {
      this.cronSettings += ":" + "SAT";
      if (prevDay) {
        this.cron += ",SAT";
      } else {
        this.cron += " SAT";
      }
      prevDay = true;
    }

    if (this.sunday) {
      this.cronSettings += ":" + "SUN";
      if (prevDay) {
        this.cron += ",SUN";
      } else {
        this.cron += " SUN";
      }
    }

    this.cron += " *";
    this.validateCron();
  }

  setMonthlyTabCron() {
    if (this.monthlyRadio == "1") {
      this.cron = this.everySecond1MonthlyTab + " " + this.everyMinute1MonthlyTab + " " + this.everyHour1MonthlyTab + " " + this.everyDay1MonthlyTab +" 1/" + this.everyMonth1MonthlyTab +" ? *";
      this.cronSettings = "Monthly:1:" + this.everySecond1MonthlyTab + ":" + this.everyMinute1MonthlyTab + ":" + this.everyHour1MonthlyTab + ":" + this.everyDay1MonthlyTab + ":" + this.everyMonth1MonthlyTab;
    } else {
      this.cron = this.everySecond2MonthlyTab + " " + this.everyMinute2MonthlyTab + " " + this.everyHour2MonthlyTab + " ? 1/" + this.everyMonthMonthlyTab + " " + this.everyDayMonthlyTab + this.everyWeekMonthlyTab + " *";
      this.cronSettings = "Monthly:2:" + this.everySecond2MonthlyTab + ":" + this.everyMinute2MonthlyTab + ":" + this.everyHour2MonthlyTab + ":" + this.everyMonthMonthlyTab + ":" + this.everyDayMonthlyTab + ":" + this.everyWeekMonthlyTab;
    }
    this.validateCron();
  }

  setYearlyTabCron() {
    if (this.yearlyRadio == "1") {
      this.cron =  this.everySecond1YearlyTab + " " + this.everyMinute1YearlyTab + " " + this.everyHour1YearlyTab + " " + this.everyDay1YearlyTab + " " + this.everyMonth1YearlyTab + " ? *";
      this.cronSettings = "Yearly:1:" + this.everySecond1YearlyTab + ":" + this.everyMinute1YearlyTab + ":" + this.everyHour1YearlyTab + ":" + this.everyDay1YearlyTab + ":" + this.everyMonth1YearlyTab;
    } else {
      this.cron = this.everySecond2YearlyTab + " " + this.everyMinute2YearlyTab + " " + this.everyHour2YearlyTab + " ? " + this.everyMonthYearlyTab + " " + this.everyDayYearlyTab + this.everyWeekYearlyTab + " *";
      this.cronSettings = "Yearly:2:" + this.everySecond2YearlyTab + ":" + this.everyMinute2YearlyTab + ":" + this.everyHour2YearlyTab + ":" + this.everyMonthYearlyTab + ":" + this.everyDayYearlyTab + ":" + this.everyWeekYearlyTab;
    }
    this.validateCron();
  }

  setAdvancedTabCron() {
    this.cron = "";
    this.cronDescription = "";
    if (this.cronManual != "") {
      if (this.cronManual != this.cron) {
        this.cron = this.cronManual;
        this.cronSettings = "Advanced";
        this.validateCron();
      }
    } else {
      this.formIsInvalid = true;
    }
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

  toggleMonthly() {
    if (this.monthlyRadio == "1") {
      this.monthlyRow1 = false;
      this.monthlyRow2 = true;
    } else {
      this.monthlyRow1 = true;
      this.monthlyRow2 = false;
    }
    this.setMonthlyTabCron();
  }

  toggleYearly() {
    if (this.yearlyRadio == "1") {
      this.yearlyRow1 = false;
      this.yearlyRow2 = true;
    } else {
      this.yearlyRow1 = true;
      this.yearlyRow2 = false;
    }
    this.setYearlyTabCron();
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
          if (this.allowTime == false && this.schedule.cronExpression.startsWith("0 0 0") == false) {
            alert("Cannot set time component. Cron must start with 0 0 0.");
            this.formIsInvalid = true;
            return;
          }
          this.schedule.cronSettings = this.cronSettings;
          if (this.schedule.cronDescription.toLowerCase().search("invalid") != -1) {
            alert("Cannot set an invalid cron schedule.");
            this.formIsInvalid = true;
            return;
          }
          this.formIsInvalid = false;
        }
      );
  }

  cancel() {
    this.dialogRef.close();
  }
}
