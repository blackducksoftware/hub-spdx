package com.blackducksoftware.integration.hub.spdx;

import java.io.File;
import java.io.PrintStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.blackducksoftware.integration.hub.spdx.hub.Hub;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportGenerator;
import com.blackducksoftware.integration.hub.spdx.hub.HubProjectVersion;

@SpringBootApplication
public class SpdxReportUtility {

    @Autowired
    Hub hub;

    @Autowired
    HubProjectVersion hubProjectVersion;

    @Autowired
    HubBomReportBuilder spdxReportBuilder;

    @Value("${output.filename}")
    private String outputFilename;

    private static final Logger logger = LoggerFactory.getLogger(SpdxReportUtility.class);

    public static void main(final String[] args) {
        new SpringApplicationBuilder(SpdxReportUtility.class).logStartupInfo(false).run(args);
    }

    @PostConstruct
    private void writeReport() {

        try {
            // Connect to Hub
            hub.connect();

            // Create a HubBomReportGenerator with SpdxHubBomReportBuilder
            final HubBomReportGenerator spdxReportGenerator = new HubBomReportGenerator(hub, spdxReportBuilder);

            // Generate an SPDX report
            final File outputFile = new File(outputFilename);
            final PrintStream ps = new PrintStream(outputFile);
            spdxReportGenerator.writeReport(ps, hubProjectVersion.getName(), hubProjectVersion.getVersion(), hub.getHubUrl());
            logger.info(String.format("Generated report file %s", outputFilename));
        } catch (final Throwable e) {
            logger.error(e.getMessage());
            final String trace = ExceptionUtils.getStackTrace(e);
            logger.debug(String.format("Stack trace: %s", trace));
            logger.info(getUsage());
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
}
