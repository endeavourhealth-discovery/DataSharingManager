create table `project_schedule` (

    uuid char(36) NOT NULL COMMENT 'Unique identifier for the schedule',
	starts date COMMENT 'Starting date when reports will be sent',
	ends date COMMENT 'Ending date when reports will no longer be sent',
	frequency smallint COMMENT 'Frequency of report sending. 0-Daily, 1-Weekly, 2-Monthly, 3-Yearly',
	weeks char(7) COMMENT 'Comma separated week setting. i.e. 1,2,3,4',
	is_monday tinyint(1) COMMENT '1 if report is to be sent on Mondays',
	is_tuesday tinyint(1) COMMENT '1 if report is to be sent on Tuesdays',
	is_wednesday tinyint(1) COMMENT '1 if report is to be sent on Wednesdays',
	is_thursday tinyint(1) COMMENT '1 if report is to be sent on Thursdays',
	is_friday tinyint(1) COMMENT '1 if report is to be sent on Fridays',
	is_saturday tinyint(1) COMMENT '1 if report is to be sent on Saturdays',
	is_sunday tinyint(1) COMMENT '1 if report is to be sent on Sundays',
    primary key data_sharing_manager_schedule_uuid (uuid)
) comment 'Information regarding project schedule';

insert into data_sharing_manager.map_type (id, map_type)
values (16, "Schedule");