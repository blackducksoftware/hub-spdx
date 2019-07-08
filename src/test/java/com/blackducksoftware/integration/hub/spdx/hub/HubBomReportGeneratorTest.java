package com.blackducksoftware.integration.hub.spdx.hub;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ListedLicenses;

import com.blackducksoftware.integration.hub.spdx.SpdxHubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

public class HubBomReportGeneratorTest {

    @Test
    public void test() throws IntegrationException, UnsupportedEncodingException, InvalidSPDXAnalysisException {
        final HubBomReportGenerator hubBomReportGenerator = new HubBomReportGenerator();
        final Hub hub = Mockito.mock(Hub.class);
        final ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        final ProjectView projectView = new ProjectView();
        projectView.name = "testProject";
        projectVersionWrapper.setProjectView(projectView);
        final ProjectVersionView projectVersionView = new ProjectVersionView();
        projectVersionView.versionName = "testVersion";
        projectVersionView._meta = new ResourceMetadata();
        projectVersionView._meta.links = new ArrayList<>();
        final ResourceLink resourceLink = new ResourceLink();
        resourceLink.rel = "components";
        resourceLink.href = "testBomUrl";
        projectVersionView._meta.links.add(resourceLink);
        projectVersionWrapper.setProjectVersionView(projectVersionView);
        final ProjectService projectService = Mockito.mock(ProjectService.class);
        Mockito.when(projectService.getProjectVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(projectVersionWrapper);
        final List<VersionBomComponentView> bom = new ArrayList<>();
        final VersionBomComponentView versionBomComponentView = new VersionBomComponentView();
        versionBomComponentView.component = "testComponent";
        versionBomComponentView.componentName = "testComponentName";
        versionBomComponentView.componentVersionName = "testComponentVersionName";
        versionBomComponentView.licenses = new ArrayList<>();
        final VersionBomLicenseView versionBomLicenseView = new VersionBomLicenseView();
        versionBomLicenseView.licenseDisplay = "testLicenseDisplay";
        versionBomLicenseView.license = "testLicense";
        versionBomComponentView.licenses.add(versionBomLicenseView);
        versionBomComponentView.usages = new ArrayList<>();

        bom.add(versionBomComponentView);
        Mockito.when(projectService.getComponentsForProjectVersion(Mockito.any(ProjectVersionView.class))).thenReturn(bom);
        Mockito.when(hub.getProjectService()).thenReturn(projectService);

        hubBomReportGenerator.setHub(hub);
        final SpdxHubBomReportBuilder spdxHubBomReportBuilder = new SpdxHubBomReportBuilder();
        final SpdxPkg spdxPkg = new SpdxPkg();
        spdxHubBomReportBuilder.setSpdxPkg(spdxPkg);
        final SpdxLicense spdxLicense = Mockito.mock(SpdxLicense.class);
        final AnyLicenseInfo apacheLicense = ListedLicenses.getListedLicenses().getListedLicenseById("Apache-2.0");
        // final AnyLicenseInfo compSpdxLicense = spdxLicense.generateLicenseInfo(bomContainer, hubGenericLicenseView);
        // spdxLicense.generateLicenseInfo(bomContainer, hubComplexLicense)
        Mockito.when(spdxLicense.generateLicenseInfo(Mockito.any(SpdxDocumentContainer.class), Mockito.any(HubGenericComplexLicenseView.class))).thenReturn(apacheLicense);
        spdxHubBomReportBuilder.setSpdxLicense(spdxLicense);
        spdxHubBomReportBuilder.setProject(projectVersionWrapper, "myProject", "myVersion", "theBomUrl");
        hubBomReportGenerator.setSpdxHubBomReportBuilder(spdxHubBomReportBuilder);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos, true, "utf-8");
        hubBomReportGenerator.writeReport(ps, "testProjectName", "testProjectVersion");
        final String report = baos.toString();
        assertTrue(report.contains("CREATIVE COMMONS CORPORATION IS NOT A LAW FIRM AND DOES NOT PROVIDE LEGAL SERVICES"));
        assertTrue(report.contains("<spdx:name>testProject</spdx:name>"));
        assertTrue(report.contains("<spdx:downloadLocation>testBomUrl</spdx:downloadLocation>"));
        assertTrue(report.contains("<spdx:versionInfo>testVersion</spdx:versionInfo>"));
        assertTrue(report.contains("<spdx:name>testProject:testVersion Bill Of Materials</spdx:name>"));
        assertTrue(report.contains("<rdfs:seeAlso>https://creativecommons.org/publicdomain/zero/1.0/legalcode</rdfs:seeAlso>"));
        assertTrue(report.contains("<spdx:licenseId>CC0-1.0</spdx:licenseId>"));

        assertTrue(report.contains("http://www.apache.org/licenses/LICENSE-2.0"));
        assertTrue(report.contains("<spdx:name>Apache License 2.0</spdx:name>"));
        assertTrue(report.contains("<spdx:licenseId>Apache-2.0</spdx:licenseId>"));
        assertTrue(report.contains("<spdx:name>testComponentName</spdx:name>"));
        assertTrue(report.contains("<spdx:versionInfo>testComponentVersionName</spdx:versionInfo>"));
    }

}
