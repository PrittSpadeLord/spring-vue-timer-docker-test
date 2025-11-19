package io.github.prittspadelord;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.web.SpringServletContainerInitializer;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class SpringVueTimerDockerTestApplication {
    static void main() {

        Properties props = new Properties();

        try {
            props.load(SpringVueTimerDockerTestApplication.class.getResourceAsStream("/application.properties"));

            String hostName = props.getProperty("ssl.hostName");
            String certificateAlias = props.getProperty("ssl.certificateAlias");
            String certificatePassword = props.getProperty("ssl.certificatePassword");

            Tomcat tomcat = new Tomcat();

            Connector connector = tomcat.getConnector();
            connector.setPort(8443);
            connector.setSecure(true);
            connector.setScheme("https");
            connector.getProtocolHandler().setExecutor(new VirtualThreadTaskExecutor());

            connector.setProperty("SSLEnabled", "true");
            connector.setProperty("defaultSSLHostConfigName", hostName);

            SSLHostConfig sslHostConfig = new SSLHostConfig();
            sslHostConfig.setHostName(hostName);
            sslHostConfig.setSslProtocol("TLSv1.3");

            SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(
                sslHostConfig,
                SSLHostConfigCertificate.Type.RSA
            );

            String relativeKeystorePath = (System.getProperty("user.dir").endsWith("/") ? "" : "/") + "certs/vuespringtimer-cert.p12";

            certificate.setCertificateKeyAlias(certificateAlias);
            certificate.setCertificateKeystoreFile(System.getProperty("user.dir") + relativeKeystorePath);
            certificate.setCertificateKeystorePassword(certificatePassword);
            certificate.setCertificateKeystoreType("PKCS12");

            sslHostConfig.addCertificate(certificate);

            connector.addSslHostConfig(sslHostConfig);
            connector.addUpgradeProtocol(new Http2Protocol());

            tomcat.setConnector(connector);

            Context tomcatContext = tomcat.addContext("", System.getProperty("user.dir"));
            tomcatContext.addServletContainerInitializer(
                new SpringServletContainerInitializer(),
                Set.of(MyDispatcherServletInitializer.class)
            );

            tomcat.start();
            tomcat.getServer().await();
        }
        catch(IOException e) {
            log.error("Error occured while loading props", e);
            throw new RuntimeException(e);
        }
        catch(LifecycleException e) {
            log.error("Error occured during Tomcat startup", e);
            throw new RuntimeException(e);
        }
    }
}