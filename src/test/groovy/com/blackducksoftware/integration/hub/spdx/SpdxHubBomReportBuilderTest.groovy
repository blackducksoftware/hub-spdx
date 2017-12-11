package com.blackducksoftware.integration.hub.spdx;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.model.enumeration.MatchedFileUsageEnum;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView
import com.blackducksoftware.integration.hub.model.view.ProjectView
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.model.view.components.OriginView;
import com.blackducksoftware.integration.hub.spdx.hub.HubLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;

public class SpdxHubBomReportBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
        final List<VersionBomComponentModel> bom = new ArrayList<>();
        final VersionBomComponentView bomCompView = new VersionBomComponentView();
        bomCompView.componentName = "OpenSSL";
        bomCompView.componentVersionName = "1.2.3";
        bomCompView.origins = new ArrayList<>();
        final OriginView origin = new OriginView();
        origin.name = "Test Origin";
        origin.externalId = "testOriginExtId";
        origin.externalNamespace = "testOriginExtNamespace";
        bomCompView.origins.add(origin);
        final List<MatchedFileUsageEnum> usages = new ArrayList<>();
        usages.add(MatchedFileUsageEnum.DYNAMICALLY_LINKED);
        bomCompView.usages = usages;
        final VersionBomComponentModel bomCompModel = new VersionBomComponentModel(bomCompView, new ArrayList<MatchedFilesView>());
        bom.add(bomCompModel);

        // Generate report for that mocked Hub project
        final SpdxHubBomReportBuilder reportBuilder = new SpdxHubBomReportBuilder();
        reportBuilder.spdxPkg = new SpdxPkg();
        reportBuilder.spdxLicense = new SpdxLicense();
        reportBuilder.spdxLicense.setHubLicense(new HubLicense());
        reportBuilder.spdxPkg.setSpdxLicense(new SpdxLicense());

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
        for (final VersionBomComponentModel bomComp : bom) {
            reportBuilder.addComponent(bomComp);
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
