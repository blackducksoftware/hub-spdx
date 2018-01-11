package com.blackducksoftware.integration.hub.spdx.hub;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.spdx.SpdxHubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.SpdxRelatedLicensedPackage;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class HubBomReportGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Hub hub;

    @Autowired
    SpdxHubBomReportBuilder reportBuilder;

    @Value("${single.thread:false}")
    private boolean singleThread;

    public String createReport(final String projectName, final String projectVersion, final String hubUrl) throws IntegrationException {
        consumeHubProjectBom(hubUrl, projectName, projectVersion);
        return reportBuilder.generateReportAsString();
    }

    public void writeReport(final PrintStream ps, final String projectName, final String projectVersion, final String hubUrl) throws IntegrationException {
        consumeHubProjectBom(hubUrl, projectName, projectVersion);
        reportBuilder.writeReport(ps);
    }

    private void consumeHubProjectBom(final String hubUrl, final String projectName, final String projectVersion) throws IntegrationException, HubIntegrationException {
        logger.info(String.format("Generating report for project %s:%s", projectName, projectVersion));
        final ProjectVersionWrapper projectVersionWrapper = hub.getProjectDataService().getProjectVersion(projectName, projectVersion);
        final String bomUrl = (new MetaHandler(new Slf4jIntLogger(logger))).getFirstLinkSafely(projectVersionWrapper.getProjectVersionView(), MetaHandler.COMPONENTS_LINK);
        reportBuilder.setProject(projectVersionWrapper, bomUrl);
        final List<VersionBomComponentView> bom = hub.getVersionBomComponentDataService().getBomEntries(projectVersionWrapper.getProjectVersionView());

        logger.info("Creating packages");
        Stream<VersionBomComponentView> bomCompStream = null;
        if (singleThread) {
            logger.info("Conversion of BOM components to SpdxPackages: Single-threaded");
            bomCompStream = bom.stream();
        } else {
            logger.info("Conversion of BOM components to SpdxPackages: Multi-threaded");
            bomCompStream = bom.parallelStream();
        }
        final List<Optional<SpdxRelatedLicensedPackage>> pkgs = bomCompStream.map(bomComp -> toSpdx(bomComp)).collect(Collectors.toList());
        logger.info("Creating packages: Done");

        logger.info("Adding packages to document");
        for (final Optional<SpdxRelatedLicensedPackage> pkg : pkgs) {
            final SpdxRelatedLicensedPackage actualPkg = pkg.orElseThrow(() -> new HubIntegrationException("Conversion to SPDX failed for one or more components"));
            reportBuilder.addPackageToDocument(actualPkg);
        }
        logger.info("Adding packages to document: Done");

    }

    private Optional<SpdxRelatedLicensedPackage> toSpdx(final VersionBomComponentView bomComp) {
        Optional<SpdxRelatedLicensedPackage> pkg = Optional.empty();
        try {
            pkg = Optional.of(reportBuilder.toSpdxRelatedLicensedPackage(bomComp));
        } catch (final IntegrationException e) {
            final String msg = String.format("Error converting BOM component %s:%s to Spdx packages: %s", bomComp.componentName, bomComp.componentVersionName, e.getMessage());
            logger.error(msg);
        }
        return pkg;
    }
}
