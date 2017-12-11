package com.blackducksoftware.integration.hub.spdx;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.license.SpdxNoneLicense;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.MatchedFileUsageEnum;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.components.OriginView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.hub.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.HubGenericLicenseViewFactory;
import com.blackducksoftware.integration.hub.spdx.hub.HubLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;

@Component
public class SpdxHubBomReportBuilder implements HubBomReportBuilder {

    @Autowired
    SpdxPkg spdxPkg;

    @Autowired
    HubLicense hubLicense;

    @Autowired
    SpdxLicense spdxLicense;

    private static final String TOOL_NAME = "Tool: Black Duck Hub SPDX Report Generator";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String SPDX_SPEC_VERSION = "SPDX-2.1";
    private SpdxDocumentContainer bomContainer;
    private SpdxDocument bomDocument;

    @Override
    public void setProject(final ProjectVersionWrapper projectVersionWrapper, final String bomUrl) throws HubIntegrationException {
        bomContainer = null;
        try {
            bomContainer = new SpdxDocumentContainer("http://blackducksoftware.com", SPDX_SPEC_VERSION);
        } catch (final InvalidSPDXAnalysisException e1) {
            throw new HubIntegrationException("Error creating SPDX container", e1);
        }
        bomDocument = bomContainer.getSpdxDocument();

        try {
            bomDocument.getCreationInfo().setCreators(new String[] { TOOL_NAME });
        } catch (final InvalidSPDXAnalysisException e) {
            throw new HubIntegrationException("Error setting creator on SPDX document", e);
        }
        bomDocument.setName(String.format("%s:%s Bill Of Materials", projectVersionWrapper.getProjectView().name, projectVersionWrapper.getProjectVersionView().versionName));

        // Document level description package
        final Relationship description = createDocumentDescription(projectVersionWrapper, bomUrl);
        try {
            bomDocument.addRelationship(description);
        } catch (final InvalidSPDXAnalysisException e) {
            throw new HubIntegrationException("Error adding describes relationship to SPDX document", e);
        }
    }

    @Override
    public void addComponent(final VersionBomComponentModel bomComp) throws IntegrationException {
        logUsages(bomComp);
        addPackage(bomDocument, bomComp);
    }

    @Override
    public String generateReportAsString() throws HubIntegrationException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = null;
        try {
            try {
                ps = new PrintStream(baos, true, "utf-8");
            } catch (final UnsupportedEncodingException e) {
                throw new HubIntegrationException("Error creating PrintStream", e);
            }
            writeReport(ps);
        } finally {
            if (ps != null) {
                ps.flush();
                ps.close();
            }
        }
        return baos.toString();
    }

    @Override
    public void writeReport(final PrintStream ps) {
        bomContainer.getModel().write(ps, "RDF/XML");
    }

    private RelationshipType getRelationshipType(final VersionBomComponentModel bomComp) {
        RelationshipType relType = RelationshipType.OTHER;
        final List<MatchedFileUsageEnum> usages = bomComp.getUsages();
        if (usages.size() > 1) {
            logger.warn(String.format("# Usages for component %s:%s is > 1: %d; only the first is used", bomComp.getComponentName(), bomComp.getComponentVersionName(), usages.size()));
        }
        if (usages.size() > 0) {
            if (usages.get(0) == MatchedFileUsageEnum.DYNAMICALLY_LINKED) {
                relType = RelationshipType.DYNAMIC_LINK;
            } else if (usages.get(0) == MatchedFileUsageEnum.STATICALLY_LINKED) {
                relType = RelationshipType.STATIC_LINK;
            } else if (usages.get(0) == MatchedFileUsageEnum.SOURCE_CODE) {
                relType = RelationshipType.GENERATED_FROM;
            } else if (usages.get(0) == MatchedFileUsageEnum.DEV_TOOL_EXCLUDED) {
                relType = RelationshipType.BUILD_TOOL_OF;
            } else if (usages.get(0) == MatchedFileUsageEnum.IMPLEMENTATION_OF_STANDARD) {
                relType = RelationshipType.DESCRIBED_BY;
            } else if (usages.get(0) == MatchedFileUsageEnum.SEPARATE_WORK) {
                relType = RelationshipType.OTHER;
            }
        }
        return relType;
    }

    private Relationship createDocumentDescription(final ProjectVersionWrapper projectVersionWrapper, final String projectDownloadLocation) {
        final String hubProjectComment = null;
        final AnyLicenseInfo licenseConcluded = new SpdxNoAssertionLicense();
        final AnyLicenseInfo[] licenseInfoInFiles = new AnyLicenseInfo[] { new SpdxNoAssertionLicense() };
        final String copyrightText = null;
        final String licenseComment = null;
        final AnyLicenseInfo licenseDeclared = getProjectVersionSpdxLicense(projectVersionWrapper);
        final SpdxPackageVerificationCode packageVerificationCode = null;
        final SpdxPackage documentDescriptionPackage = new SpdxPackage(projectVersionWrapper.getProjectView().name, hubProjectComment, new Annotation[0], new Relationship[0], licenseConcluded, licenseInfoInFiles, copyrightText,
                licenseComment, licenseDeclared, new Checksum[0], projectVersionWrapper.getProjectView().description, projectDownloadLocation, new SpdxFile[0], "http://www.blackducksoftware.com", projectDownloadLocation, null,
                packageVerificationCode, null, null, null, projectVersionWrapper.getProjectVersionView().versionName);
        documentDescriptionPackage.setCopyrightText("NOASSERTION");
        documentDescriptionPackage.setSupplier("NOASSERTION");
        documentDescriptionPackage.setOriginator("NOASSERTION");
        documentDescriptionPackage.setFilesAnalyzed(false);
        final Relationship describes = new Relationship(documentDescriptionPackage, RelationshipType.DESCRIBES, "top level comment");
        return describes;
    }

    private AnyLicenseInfo getProjectVersionSpdxLicense(final ProjectVersionWrapper projectVersionWrapper) {
        AnyLicenseInfo licenseDeclared = new SpdxNoneLicense();
        final ComplexLicenseView license = projectVersionWrapper.getProjectVersionView().license;
        if (license == null) {
            logger.warn("The Hub provided no license information for the project version");
            return licenseDeclared;
        }
        final HubGenericComplexLicenseView hubGenericLicenseView = HubGenericLicenseViewFactory.create(license);
        try {
            licenseDeclared = spdxLicense.generateLicenseInfo(bomContainer, hubGenericLicenseView);
        } catch (final IntegrationException e) {
            logger.error(String.format("Unable to generate license information for the project: %s", e.getMessage()));
        }
        return licenseDeclared;
    }

    private void addPackage(final SpdxDocument bomDocument, final VersionBomComponentModel bomComp) throws IntegrationException {

        HubGenericComplexLicenseView hubGenericLicenseView = null;
        final List<VersionBomLicenseView> licenses = bomComp.getLicenses();
        if (licenses == null) {
            logger.warn(String.format("The Hub provided no license information for BOM component %s/%s", bomComp.getComponentName(), bomComp.getComponentVersionName()));
        } else {
            logger.debug(String.format("Component %s:%s", bomComp.getComponentName(), bomComp.getComponentVersionName()));
            hubGenericLicenseView = HubGenericLicenseViewFactory.create(licenses.get(0));
        }
        final AnyLicenseInfo compSpdxLicense = spdxLicense.generateLicenseInfo(bomContainer, hubGenericLicenseView);
        logger.debug(String.format("Creating package for %s:%s", bomComp.getComponentName(), bomComp.getComponentVersionName()));
        final String bomCompDownloadLocation = "NOASSERTION";
        final RelationshipType relType = getRelationshipType(bomComp);
        spdxPkg.addPackageToDocument(bomDocument, compSpdxLicense, bomComp.getComponentName(), bomComp.getComponentVersionName(), bomCompDownloadLocation, relType);
    }

    private void logUsages(final VersionBomComponentModel bomComp) {
        final List<OriginView> origins = bomComp.getOrigins();
        logger.debug(String.format("# Origins: %d", origins.size()));
        for (final OriginView origin : origins) {
            logger.debug(String.format("\tOrigin: externalNamespace=%s, externalId=%s, name=%s", origin.externalNamespace, origin.externalId, origin.name));
        }
    }
}
