package com.blackducksoftware.integration.hub.spdx;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;

public class HubBomReportGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final ProjectDataService projectDataService;
    final VersionBomComponentDataService versionBomComponentDataService;
    final HubBomReportBuilder reportBuilder;

    public HubBomReportGenerator(final ProjectDataService projectDataService, final VersionBomComponentDataService versionBomComponentDataService, final HubBomReportBuilder reportBuilder) {
        this.projectDataService = projectDataService;
        this.versionBomComponentDataService = versionBomComponentDataService;
        this.reportBuilder = reportBuilder;
    }

    public String createReport(final String projectName, final String projectVersion, final String hubUrl) throws IntegrationException {

        final ProjectVersionWrapper projectVersionWrapper = projectDataService.getProjectVersion(projectName, projectVersion);
        final String projectUrl = hubUrl; // TODO
        reportBuilder.setProject(projectName, projectVersion, projectUrl);
        logger.info("Traversing BOM");
        final List<VersionBomComponentModel> bom = versionBomComponentDataService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        for (final VersionBomComponentModel bomComp : bom) {
            reportBuilder.addComponent(bomComp);
        }

        return reportBuilder.generateReportAsString();
    }
}
