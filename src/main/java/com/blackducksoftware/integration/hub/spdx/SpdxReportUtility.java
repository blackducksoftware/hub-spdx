package com.blackducksoftware.integration.hub.spdx;

import java.io.File;
import java.io.PrintStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportGenerator;
import com.blackducksoftware.integration.hub.spdx.hub.HubPassword;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@SpringBootApplication
public class SpdxReportUtility {

    @Autowired
    private HubPassword hubPassword;

    @Value("${hub.url}")
    private String hubUrl;

    @Value("${hub.username}")
    private String hubUsername;

    @Value("${hub.timeout}")
    private int hubTimeoutSeconds;

    @Value("${hub.always.trust.cert}")
    private boolean hubAlwaysTrustCert;

    @Value("${hub.project.version}")
    private String hubProjectVersion;

    @Value("${hub.project.name}")
    private String hubProjectName;

    @Value("${output.filename}")
    private String outputFilename;

    private static final Logger logger = LoggerFactory.getLogger(SpdxReportUtility.class);

    public static void main(final String[] args) {
        new SpringApplicationBuilder(SpdxReportUtility.class).logStartupInfo(false).run(args);
    }

    @PostConstruct
    private void writeReport() {

        try {
            validateProperties();

            // Connect to Hub
            final HubServerConfig hubServerConfig = createBuilder().build();
            final CredentialsRestConnection restConnection;
            restConnection = hubServerConfig.createCredentialsRestConnection(new Slf4jIntLogger(logger));
            restConnection.connect();
            final HubServicesFactory hubSvcsFactory = new HubServicesFactory(restConnection);
            final ProjectDataService projectDataService = hubSvcsFactory.createProjectDataService();
            final VersionBomComponentDataService versionBomComponentDataService = hubSvcsFactory.createVersionBomComponentDataservice();
            final MetaService metaService = hubSvcsFactory.createMetaService();

            // Create a HubBomReportGenerator with SpdxHubBomReportBuilder
            final HubBomReportBuilder spdxReportBuilder = new SpdxHubBomReportBuilder();
            final HubBomReportGenerator spdxReportGenerator = new HubBomReportGenerator(projectDataService, versionBomComponentDataService, metaService, spdxReportBuilder);

            // Generate an SPDX report
            final File outputFile = new File(outputFilename);
            final PrintStream ps = new PrintStream(outputFile);
            spdxReportGenerator.writeReport(ps, hubProjectName, hubProjectVersion, hubUrl);
            logger.info(String.format("Generated report file %s", outputFilename));
        } catch (final Throwable e) {
            logger.error(e.getMessage());
            logger.info(getUsage());
        }
    }

    private void validateProperties() throws HubIntegrationException {
        if (StringUtils.isBlank(hubUrl)) {
            throw new HubIntegrationException("Property hub.url is required");
        }
        if (StringUtils.isBlank(hubUsername)) {
            throw new HubIntegrationException("Property hub.username is required");
        }
        if (StringUtils.isBlank(hubPassword.get())) {
            throw new HubIntegrationException("Property hub.password is required");
        }
        if (StringUtils.isBlank(hubProjectVersion)) {
            throw new HubIntegrationException("Property hub.project.version is required");
        }
        if (StringUtils.isBlank(hubProjectName)) {
            throw new HubIntegrationException("Property hub.project.name is required");
        }
        if (StringUtils.isBlank(outputFilename)) {
            throw new HubIntegrationException("Property output.filename is required");
        }

    }

    private String getUsage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\nUsage: java -jar hub-spdx-<version>.jar <arguments>\n");
        sb.append("Required arguments:\n");
        sb.append("\t--hub.url=<Hub URL>\n");
        sb.append("\t--hub.username=<Hub username>\n");
        sb.append("\t--hub.project.name=<Hub project name>\n");
        sb.append("\t--hub.project.version=<Hub project version>\n");
        sb.append("\t--output.filename=<path to report file>\n");
        sb.append("\n");
        sb.append("Optional arguments:\n");
        sb.append("\t--hub.password=<Hub password>\n");
        sb.append("\t--hub.always.trust.cert=true\n");
        sb.append("\n");
        sb.append("Optional environment variable:\n");
        sb.append("\texport BD_HUB_PASSWORD=<password>\n");
        sb.append("\n");
        sb.append("Documentation: https://github.com/blackducksoftware/hub-spdx/blob/master/README.md\n");

        return sb.toString();
    }

    private HubServerConfigBuilder createBuilder() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword.get());
        hubServerConfigBuilder.setTimeout(hubTimeoutSeconds);
        hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubAlwaysTrustCert);
        return hubServerConfigBuilder;
    }

}
