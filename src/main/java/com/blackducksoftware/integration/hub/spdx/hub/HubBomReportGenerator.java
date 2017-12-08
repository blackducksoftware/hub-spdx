package com.blackducksoftware.integration.hub.spdx.hub;

import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class HubBomReportGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Hub hub;

    @Autowired
    HubBomReportBuilder reportBuilder;

    @Autowired
    HubLicense hubLicense;

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
        logProjectVersionLicense(projectVersionWrapper);
        final String bomUrl = (new MetaService(new Slf4jIntLogger(logger))).getFirstLinkSafely(projectVersionWrapper.getProjectVersionView(), MetaService.COMPONENTS_LINK);
        reportBuilder.setProject(projectName, projectVersion, projectVersionWrapper.getProjectView().description, bomUrl);
        logger.debug("Traversing BOM");
        final List<VersionBomComponentModel> bom = hub.getVersionBomComponentDataService().getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        for (final VersionBomComponentModel bomComp : bom) {
            reportBuilder.addComponent(bomComp);
        }
    }

    // TODO
    private void logProjectVersionLicense(final ProjectVersionWrapper projectVersionWrapper) {
        final ComplexLicenseView hubProjectVersionLicense = projectVersionWrapper.getProjectVersionView().license;
        if (hubProjectVersionLicense == null) {
            logger.info("Hub Project Version has no license");
            return;
        }
        logger.info(String.format("Hub Project Version License URL: %s", hubProjectVersionLicense.license));
        if (hubProjectVersionLicense.type != null) {
            logger.info(String.format("Hub Project Version License type: %s", hubProjectVersionLicense.type.toString()));
        }
        logger.info(String.format("Hub Project Version License display: %s", hubProjectVersionLicense.licenseDisplay));
        logger.info(String.format("Hub Project Version License name: %s", hubProjectVersionLicense.name));
        logger.info(String.format("Hub Project Version License # licenses: %d", hubProjectVersionLicense.licenses.size()));
        try {
            final LicenseView hubProjectVersionLicenseView = hubLicense.getLicenseView(hubProjectVersionLicense);
            if (hubProjectVersionLicenseView == null) {
                logger.info("Hub Project Version complex license has no license view");
            } else {
                logger.info(String.format("Hub Project Version License name: %s", hubProjectVersionLicenseView.name));
            }
        } catch (final IntegrationException e) {
            logger.warn("Error getting Hub Project Version license");
        }
        for (final ComplexLicenseView lic : hubProjectVersionLicense.licenses) {
            logger.info(String.format("\tsub License URL: %s", lic.license));
            if (lic.type != null) {
                logger.info(String.format("\tsub License type: %s", lic.type.toString()));
            }
            logger.info(String.format("\tsub License display: %s", lic.licenseDisplay));
            logger.info(String.format("\tsub License name: %s", lic.name));
            try {
                final LicenseView subLicenseView = hubLicense.getLicenseView(lic);
                if (subLicenseView == null) {
                    logger.info("Hub Project Version complex license has no license view");
                } else {
                    logger.info(String.format("Hub Project Version License name: %s", subLicenseView.name));
                }
            } catch (final IntegrationException e) {
                logger.warn("Error getting Hub Project Version sub license");
            }
        }
    }
}
