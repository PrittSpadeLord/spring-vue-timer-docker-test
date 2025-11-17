package io.github.prittspadelord;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.web.SpringServletContainerInitializer;

import java.io.File;
import java.util.Set;

@Slf4j
public class SpringVueTimerDockerTestApplication {
    static void main() {
        Tomcat tomcat = new Tomcat();

        Connector connector = tomcat.getConnector();
        connector.setPort(8080);
        connector.getProtocolHandler().setExecutor(new VirtualThreadTaskExecutor());
        tomcat.setConnector(connector);

        Context tomcatContext = tomcat.addContext("", (new File(".")).getAbsolutePath());
        tomcatContext.addServletContainerInitializer(
            new SpringServletContainerInitializer(),
            Set.of(MyDispatcherServletInitializer.class)
        );

        try {
            tomcat.start();
            tomcat.getServer().await();
        }
        catch(LifecycleException e) {
            log.error("Error occured during Tomcat startup", e);
            throw new RuntimeException(e);
        }
    }
}