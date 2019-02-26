package org.endeavourhealth.datasharingmanager.api.framework;

import com.codahale.metrics.jvm.*;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import org.endeavourhealth.coreui.framework.StartupConfig;
import org.endeavourhealth.coreui.framework.config.ConfigService;
import org.endeavourhealth.datasharingmanager.api.metrics.DataSharingManagerInstrumentedFilterContextListener;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.lang.management.ManagementFactory;

public class SwaggerBootstrap extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        Info info = new Info()
                .title("Information Model API")
                .description("Information Model API");

        System.out.println("API is running!!!");

        String baseAuthUrl = ConfigService.instance().getAuthConfig().getAuthServerUrl() +
                "/realms/" + ConfigService.instance().getAuthConfig().getRealm() + "/protocol/openid-connect";

        Swagger swagger = new Swagger().info(info);
        swagger.basePath("/api");
        swagger.securityDefinition("oauth",
                new OAuth2Definition()
                        .accessCode(baseAuthUrl + "/auth", baseAuthUrl + "/token")
                );
        new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);

        DataSharingManagerInstrumentedFilterContextListener.REGISTRY.register("Garbage Collection", new GarbageCollectorMetricSet());
        DataSharingManagerInstrumentedFilterContextListener.REGISTRY.register("Buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        DataSharingManagerInstrumentedFilterContextListener.REGISTRY.register("Memory", new MemoryUsageGaugeSet());
        DataSharingManagerInstrumentedFilterContextListener.REGISTRY.register("Threads", new ThreadStatesGaugeSet());
        DataSharingManagerInstrumentedFilterContextListener.REGISTRY.register("File Descriptor", new FileDescriptorRatioGauge());


        StartupConfig.registerShutdownHook("Dropwizard", () -> {
            System.out.println("Closing drop wizard metrics...");
            DataSharingManagerInstrumentedFilterContextListener.REGISTRY.remove("Garbage Collection");
            DataSharingManagerInstrumentedFilterContextListener.REGISTRY.remove("Buffers");
            DataSharingManagerInstrumentedFilterContextListener.REGISTRY.remove("Memory");
            DataSharingManagerInstrumentedFilterContextListener.REGISTRY.remove("Threads");
            DataSharingManagerInstrumentedFilterContextListener.REGISTRY.remove("File Descriptor");
            System.out.println("drop wizard metrics closed");
        });
    }
}
