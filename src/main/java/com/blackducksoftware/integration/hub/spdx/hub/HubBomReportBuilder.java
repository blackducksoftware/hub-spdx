package com.blackducksoftware.integration.hub.spdx.hub;

import java.io.PrintStream;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.spdx.SpdxRelatedLicensedPackage;

public interface HubBomReportBuilder {
    void setProject(ProjectVersionWrapper projectVersionWrapper, String bomUrl) throws HubIntegrationException;

    String generateReportAsString() throws HubIntegrationException;

    void writeReport(PrintStream ps);

    SpdxRelatedLicensedPackage toSpdxRelatedLicensedPackage(VersionBomComponentModel bomComp) throws IntegrationException;

    void addPackageToDocument(SpdxRelatedLicensedPackage pkg);
}
