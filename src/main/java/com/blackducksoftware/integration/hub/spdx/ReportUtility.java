package com.blackducksoftware.integration.hub.spdx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@SpringBootApplication
public class ReportUtility {

    @Value("${hub.url}")
    private String hubUrl;

    @Value("${hub.username}")
    private String hubUsername;

    @Value("${hub.password}")
    private String hubPassword;

    @Value("${hub.timeout}")
    private int hubTimeoutSeconds;

    @Value("${hub.always.trust.cert}")
    private boolean hubAlwaysTrustCert;

    @Value("${hub.project.version}")
    private String hubProjectVersion;

    @Value("${hub.project.name}")
    private String hubProjectName;

    private static final Logger logger = LoggerFactory.getLogger(ReportUtility.class);

    public static void main(final String[] args) {
        new SpringApplicationBuilder(ReportUtility.class).logStartupInfo(false).run(args);
    }

    @PostConstruct
    private void writeReport() throws IntegrationException, FileNotFoundException {

        // Connect to Hub
        final HubServerConfig hubServerConfig = createBuilder().build();
        final CredentialsRestConnection restConnection;
        restConnection = hubServerConfig.createCredentialsRestConnection(new Slf4jIntLogger(logger));
        restConnection.connect();
        final HubServicesFactory hubSvcsFactory = new HubServicesFactory(restConnection);
        final ProjectDataService projectDataService = hubSvcsFactory.createProjectDataService();
        final VersionBomComponentDataService versionBomComponentDataService = hubSvcsFactory.createVersionBomComponentDataservice();

        // Create a HubBomReportGenerator with SpdxHubBomReportBuilder
        final HubBomReportBuilder spdxReportBuilder = new SpdxHubBomReportBuilder();
        final HubBomReportGenerator spdxReportGenerator = new HubBomReportGenerator(projectDataService, versionBomComponentDataService, spdxReportBuilder);

        // Generate an SPDX report
        final File outputFile = new File(String.format("/tmp/%s_%s_bom.rdf", hubProjectName, hubProjectVersion));
        final PrintStream ps = new PrintStream(outputFile);
        spdxReportGenerator.writeReport(ps, hubProjectName, hubProjectVersion, hubUrl);
    }

    private HubServerConfigBuilder createBuilder() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);
        hubServerConfigBuilder.setTimeout(hubTimeoutSeconds);
        hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubAlwaysTrustCert);
        return hubServerConfigBuilder;
    }

}
