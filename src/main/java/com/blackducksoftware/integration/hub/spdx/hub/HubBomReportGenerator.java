package com.blackducksoftware.integration.hub.spdx.hub;

import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class HubBomReportGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final ProjectDataService projectDataService;
    final VersionBomComponentDataService versionBomComponentDataService;
    final MetaService metaService = new MetaService(new Slf4jIntLogger(logger));
    final HubBomReportBuilder reportBuilder;

    public HubBomReportGenerator(final Hub hub, final HubBomReportBuilder reportBuilder) {
        this.projectDataService = hub.getProjectDataService();
        this.versionBomComponentDataService = hub.getVersionBomComponentDataService();
        this.reportBuilder = reportBuilder;
    }

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
        final ProjectVersionWrapper projectVersionWrapper = projectDataService.getProjectVersion(projectName, projectVersion);
        final String bomUrl = metaService.getFirstLinkSafely(projectVersionWrapper.getProjectVersionView(), MetaService.COMPONENTS_LINK);
        reportBuilder.setProject(projectName, projectVersion, projectVersionWrapper.getProjectView().description, bomUrl);
        logger.debug("Traversing BOM");
        final List<VersionBomComponentModel> bom = versionBomComponentDataService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        for (final VersionBomComponentModel bomComp : bom) {
            reportBuilder.addComponent(bomComp);
        }
    }
}
