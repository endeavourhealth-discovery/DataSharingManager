package org.endeavourhealth.scheduler.api.logic;

import net.redhogs.cronparser.CronExpressionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.scheduler.api.json.JsonSchedule;
import org.quartz.CronScheduleBuilder;

import javax.ws.rs.core.Response;
import java.util.UUID;

public class SchedulerLogic {

    public Response getDescription(JsonSchedule schedule) throws Exception {

        if (StringUtils.isEmpty(schedule.getUuid())) {
            schedule.setUuid(UUID.randomUUID().toString());
        }

        if (StringUtils.isNotEmpty(schedule.getCronExpression())) {

            String description;
            try {
                CronScheduleBuilder.cronSchedule(schedule.getCronExpression());
                description = CronExpressionDescriptor.getDescription(schedule.getCronExpression());
            } catch (Exception e) {
                description = e.getMessage();
            }
            schedule.setCronDescription(description);
            return Response
                    .ok()
                    .entity(schedule)
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
