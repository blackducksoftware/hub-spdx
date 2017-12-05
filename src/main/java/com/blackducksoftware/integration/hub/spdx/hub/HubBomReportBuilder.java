package com.blackducksoftware.integration.hub.spdx.hub;

import java.io.PrintStream;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public interface HubBomReportBuilder {
    void setProject(String projectName, String projectVersion, String projectUrl) throws HubIntegrationException;

    String generateReportAsString() throws HubIntegrationException;

    void writeReport(PrintStream ps);

    void addComponent(VersionBomComponentModel bomComp) throws IntegrationException;
}
