package org.endeavourhealth.scheduler.api.logic;

import net.redhogs.cronparser.CronExpressionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;

import javax.ws.rs.core.Response;

public class SchedulerLogic {

    public Response getDescription(String cron) throws Exception {

        if (StringUtils.isNotEmpty(cron)) {

            String description;
            try {
                CronScheduleBuilder.cronSchedule(cron);
                description = CronExpressionDescriptor.getDescription(cron);
            } catch (Exception e) {
                description = e.getMessage();
            }

            return Response
                    .ok()
                    .entity(description)
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
