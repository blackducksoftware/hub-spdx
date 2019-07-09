package com.blackducksoftware.integration.hub.spdx;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.spdx.hub.license.HubLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomOriginView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

public class SpdxHubBomReportBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        final File testDir = new File("test");
        if (!testDir.exists()) {
            testDir.mkdir();
        }
    }

    @Test
    public void testWithoutLicenses() throws IOException, IntegrationException {
        test("src/test/resources/expectedSpdxWithoutLicenses.rdf", false, false);
    }

    @Test
    public void testWithLicenses() throws IOException, IntegrationException {
        test("src/test/resources/expectedSpdxWithLicensesNoSpdxOrgLicenseData.rdf", true, false);
    }

    @Test
    public void testWithLicensesUsingSpdxOrgLicenseData() throws IOException, IntegrationException {
        test("src/test/resources/expectedSpdxWithLicensesUsingSpdxOrgLicenseData.rdf", true, true);
    }

    private void test(final String expectedRdfFilePath, final boolean includeLicenseInfo, final boolean useSpdxOrgLicenseData) throws IOException, IntegrationException {
        // Mock up a hub project
        final String bomUrl = "http://hub.mydomain.com/stuff";
        final String projectName = "testProject";
        final String projectVersion = "testProjectVersion";
        final String projectDescription = "testProjectDescription";
        final List<VersionBomComponentView> bom = new ArrayList<>();
        final VersionBomComponentView bomCompView = new VersionBomComponentView();
        bomCompView.setComponentName("OpenSSL");
        bomCompView.setComponentVersionName("1.2.3");

        bomCompView.setOrigins(new ArrayList<>());

        final VersionBomOriginView origin = new VersionBomOriginView();
        origin.setName("Test Origin");
        origin.setExternalId("testOriginExtId");
        origin.setExternalNamespace("testOriginExtNamespace");
        final List<VersionBomOriginView> origins = new ArrayList<>();
        origins.add(origin);
        bomCompView.setOrigins(origins);
        final List<MatchedFileUsagesType> usages = new ArrayList<>();
        usages.add(MatchedFileUsagesType.DYNAMICALLY_LINKED);
        bomCompView.setUsages(usages);

        final VersionBomLicenseView versionBomLicenseView = new VersionBomLicenseView();
        versionBomLicenseView.setLicense("https://int-hub04.dc1.lan/api/projects/695578bf-ccca-490d-9203-44fd8d5ead6e/versions/8a21a5a2-5567-48c6-8c04-2ae9afe6a0d9/components/dc3dee66-4939-4dea-b22f-ead288b4f117/versions/f9e2e6ff-7340-4fb3-a29f-a6fa98a10bfe/licenses/7cae335f-1193-421e-92f1-8802b4243e93");
        versionBomLicenseView.setLicenseDisplay("MIT License");

        final List<VersionBomLicenseView> licenses = new ArrayList<>();
        licenses.add(versionBomLicenseView);
        bomCompView.setLicenses(licenses);

        bom.add(bomCompView);

        // Generate report for that mocked Hub project
        final SpdxHubBomReportBuilder reportBuilder = new SpdxHubBomReportBuilder();
        reportBuilder.setSpdxPkg(new SpdxPkg());
        final SpdxLicense spdxLicense = new SpdxLicense();
        reportBuilder.setSpdxLicense(spdxLicense);
        spdxLicense.setIncludeLicenses(includeLicenseInfo);
        spdxLicense.setUseSpdxOrgLicenseData(useSpdxOrgLicenseData);

        final HubLicense hubLicense = Mockito.mock(HubLicense.class);
        final LicenseView licenseView = new LicenseView();
        licenseView.setName("Apache License 2.0");
        licenseView.setSpdxId("Apache-2.0");

        Mockito.when(hubLicense.getLicenseView(Mockito.any(Optional.class))).thenReturn(licenseView);
        Mockito.when(hubLicense.getLicenseText(licenseView)).thenReturn("MIT License text blah, blah, blah...");
        spdxLicense.setHubLicense(hubLicense);

        final ProjectView projectView = new ProjectView();
        projectView.setName(projectName);
        projectView.setDescription(projectDescription);
        final ProjectVersionView projectVersionView = new ProjectVersionView();
        projectVersionView.setVersionName(projectVersion);

        final ProjectVersionWrapper projectVersionWrapper = Mockito.mock(ProjectVersionWrapper.class);
        Mockito.when(projectVersionWrapper.getProjectView()).thenReturn(projectView);
        Mockito.when(projectVersionWrapper.getProjectVersionView()).thenReturn(projectVersionView);

        reportBuilder.setProject(projectVersionWrapper, "my project report", "my version report", bomUrl);
        for (final VersionBomComponentView bomComp : bom) {
            final SpdxRelatedLicensedPackage pkg = reportBuilder.toSpdxRelatedLicensedPackage(bomComp);
            reportBuilder.addPackageToDocument(pkg);
        }
        final File actualSpdxFile = new File("test/actualSpdx.rdf");
        final PrintStream ps = new PrintStream(actualSpdxFile);
        reportBuilder.writeReport(ps);
        ps.flush();
        ps.close();
        final File expectedSpdxFile = new File(expectedRdfFilePath);
        final List<String> exceptLinesContainingThese = new ArrayList<>();
        exceptLinesContainingThese.add("spdx:created");
        final boolean match = TestUtils.contentEquals(expectedSpdxFile, actualSpdxFile, exceptLinesContainingThese);
        assertTrue(match);
        System.out.println(String.format("RDF:\n%s", FileUtils.readFileToString(actualSpdxFile, StandardCharsets.UTF_8)));
    }

}
