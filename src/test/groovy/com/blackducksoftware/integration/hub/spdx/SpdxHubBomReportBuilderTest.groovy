package com.blackducksoftware.integration.hub.spdx;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.VersionBomOriginView
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper
import com.blackducksoftware.integration.hub.spdx.hub.license.HubLicense
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;

public class SpdxHubBomReportBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        File testDir = new File("test");
        if (!testDir.exists()) {
            testDir.mkdir();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws IOException, IntegrationException {
        // Mock up a hub project
        final String bomUrl = "http://hub.mydomain.com/stuff"
        final String projectName = "testProject";
        final String projectVersion = "testProjectVersion";
        final String projectDescription = "testProjectDescription";
        final List<VersionBomComponentView> bom = new ArrayList<>();
        final VersionBomComponentView bomCompView = new VersionBomComponentView();
        bomCompView.componentName = "OpenSSL";
        bomCompView.componentVersionName = "1.2.3";
        bomCompView.origins = new ArrayList<>();
        final VersionBomOriginView origin = new VersionBomOriginView();
        origin.name = "Test Origin";
        origin.externalId = "testOriginExtId";
        origin.externalNamespace = "testOriginExtNamespace";
        bomCompView.origins.add(origin);
        final List<MatchedFileUsagesType> usages = new ArrayList<>();
        usages.add(MatchedFileUsagesType.DYNAMICALLY_LINKED);
        bomCompView.usages = usages;
        bom.add(bomCompView);

        // Generate report for that mocked Hub project
        final SpdxHubBomReportBuilder reportBuilder = new SpdxHubBomReportBuilder();
        reportBuilder.spdxPkg = new SpdxPkg();
        reportBuilder.spdxLicense = new SpdxLicense();
        reportBuilder.spdxLicense.setHubLicense(new HubLicense());

        final ProjectView projectView = new ProjectView();
        projectView.name = projectName
        projectView.description = projectDescription
        final ProjectVersionView projectVersionView = new ProjectVersionView();
        projectVersionView.versionName = projectVersion;
        final ProjectVersionWrapper projectVersionWrapper = [
            getProjectView : { projectView },
            getProjectVersionView: { projectVersionView }
        ]  as ProjectVersionWrapper
        reportBuilder.setProject(projectVersionWrapper, bomUrl);
        for (final VersionBomComponentView bomComp : bom) {
            SpdxRelatedLicensedPackage pkg = reportBuilder.toSpdxRelatedLicensedPackage(bomComp)
            reportBuilder.addPackageToDocument(pkg);
        }
        final File actualSpdxFile = new File("test/actualSpdx1.rdf");
        final PrintStream ps = new PrintStream(actualSpdxFile);
        reportBuilder.writeReport(ps);
        ps.flush();
        ps.close();
        final File expectedSpdxFile = new File("src/test/resources/expectedSpdx1.rdf");
        final List<String> exceptLinesContainingThese = new ArrayList<>();
        exceptLinesContainingThese.add("spdx:created");
        final boolean match = TestUtils.contentEquals(expectedSpdxFile, actualSpdxFile, exceptLinesContainingThese);
        assertTrue(match);
        System.out.println(String.format("RDF:\n%s", FileUtils.readFileToString(actualSpdxFile, StandardCharsets.UTF_8)));
    }

}
