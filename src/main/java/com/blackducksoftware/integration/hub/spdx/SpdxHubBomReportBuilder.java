package com.blackducksoftware.integration.hub.spdx;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;
import com.blackducksoftware.integration.hub.model.enumeration.MatchedFileUsageEnum;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.hub.model.view.components.OriginView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.hub.HubLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;

@Component
public class SpdxHubBomReportBuilder implements HubBomReportBuilder {

    @Autowired
    private HubLicense hubLicense;

    @Value("${include.licenses:false}")
    private boolean includeLicenses;

    private static final String TOOL_NAME = "Tool: Black Duck Hub SPDX Report Generator";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String SPDX_SPEC_VERSION = "SPDX-2.1";
    private SpdxDocumentContainer bomContainer;
    private SpdxDocument bomDocument;

    @Override
    public void setProject(final String projectName, final String projectVersion, final String projectUrl) throws HubIntegrationException {
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
        bomDocument.setName(String.format("%s:%s Bill Of Materials", projectName, projectVersion));

        // Document level description package
        final Relationship description = createDocumentDescription(projectName, projectVersion, projectUrl);
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
            // TODO What if there are >1 Usages??
        }
        return relType;
    }

    private Relationship createDocumentDescription(final String projectName, final String projectVersion, final String projectDownloadLocation) {
        final String hubProjectComment = null;
        final String hubProjectDescription = String.format("Black Duck Hub Project %s:%s", projectName, projectVersion);
        final AnyLicenseInfo licenseConcluded = new SpdxNoAssertionLicense();
        final AnyLicenseInfo[] licenseInfoInFiles = new AnyLicenseInfo[] { new SpdxNoAssertionLicense() };
        final String copyrightText = null;
        final String licenseComment = null;
        final AnyLicenseInfo licenseDeclared = new SpdxNoAssertionLicense();
        final SpdxPackageVerificationCode packageVerificationCode = null;
        final SpdxPackage documentDescriptionPackage = new SpdxPackage(projectName, hubProjectComment, new Annotation[0], new Relationship[0], licenseConcluded, licenseInfoInFiles, copyrightText, licenseComment, licenseDeclared,
                new Checksum[0], hubProjectDescription, projectDownloadLocation, new SpdxFile[0], "http://www.blackducksoftware.com", projectDownloadLocation, null, packageVerificationCode, null, null, null, projectVersion);
        documentDescriptionPackage.setCopyrightText("NOASSERTION");
        documentDescriptionPackage.setSupplier("NOASSERTION");
        documentDescriptionPackage.setOriginator("NOASSERTION");
        documentDescriptionPackage.setFilesAnalyzed(false);
        final Relationship describes = new Relationship(documentDescriptionPackage, RelationshipType.DESCRIBES, "top level comment");
        return describes;
    }

    // TODO do we want to call data services here?
    private void addPackage(final SpdxDocument bomDocument, final VersionBomComponentModel bomComp) throws IntegrationException {
        final RelationshipType relType = getRelationshipType(bomComp);
        final AnyLicenseInfo spdxLicense = generateLicenseInfo(bomComp);
        final String bomCompDownloadLocation = "NOASSERTION";
        String licenseId = "<none>";
        if (spdxLicense instanceof ExtractedLicenseInfo) {
            licenseId = ((ExtractedLicenseInfo) spdxLicense).getLicenseId();
        }
        logger.debug(String.format("jjjjjjjjj Creating package for %s:%s [License: %s]", bomComp.getComponentName(), bomComp.getComponentVersionName(), licenseId));
        final SpdxPackage pkg = SpdxPkg.addPackageToDocument(bomDocument, spdxLicense, bomComp.getComponentName(), bomCompDownloadLocation, relType);
        pkg.setVersionInfo(bomComp.getComponentVersionName());
        pkg.setFilesAnalyzed(false);
        pkg.setCopyrightText("NOASSERTION");
    }

    private AnyLicenseInfo generateLicenseInfo(final VersionBomComponentModel bomComp) throws IntegrationException {
        AnyLicenseInfo componentLicense = new SpdxNoAssertionLicense();
        if (!includeLicenses) {
            return componentLicense;
        }
        final List<VersionBomLicenseView> licenses = bomComp.getLicenses();
        if (licenses == null) {
            logger.warn(String.format("The Hub provided no license information for BOM component %s/%s", bomComp.getComponentName(), bomComp.getComponentVersionName()));
            return componentLicense;
        }
        logger.info(String.format("Component %s:%s", bomComp.getComponentName(), bomComp.getComponentVersionName()));
        final VersionBomLicenseView versionBomLicenseView = licenses.get(0);
        logger.info(String.format("\tlicense url: %s", versionBomLicenseView.license));
        final LicenseView licenseView = hubLicense.getLicenseView(versionBomLicenseView);
        componentLicense = createSpdxLicense(versionBomLicenseView, licenseView);
        return componentLicense;
    }

    private AnyLicenseInfo createSpdxLicense(final VersionBomLicenseView versionBomLicenseView, final LicenseView licenseView) throws IntegrationException {
        logger.info("========== CREATING SIMPLE, CONJUNCTIVE, OR DISJUNCTIVE LICENSE ========");
        AnyLicenseInfo componentLicense;
        if (versionBomLicenseView.licenseType == null) {
            componentLicense = createSimpleSpdxLicense(licenseView);
        } else {
            componentLicense = createComboSpdxLicense(versionBomLicenseView);
        }
        return componentLicense;
    }

    private AnyLicenseInfo createComboSpdxLicense(final VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        logger.info("========== CREATING CONJUNCTIVE, OR DISJUNCTIVE LICENSE ========");
        logger.info(String.format("\tlicense (%s) display: %s", versionBomLicenseView.licenseType.toString(), versionBomLicenseView.licenseDisplay));
        final List<AnyLicenseInfo> subSpdxLicenses = new ArrayList<>();
        for (final VersionBomLicenseView subLicenseVersionBomLicenseView : versionBomLicenseView.licenses) {
            logger.info(String.format("\t\tsub license url: %s", subLicenseVersionBomLicenseView.license));
            logger.info(String.format("\t\tsub license display: %s", subLicenseVersionBomLicenseView.licenseDisplay));
            // Get license text for component of license
            final LicenseView subLicenseView = hubLicense.getLicenseView(subLicenseVersionBomLicenseView);
            if (subLicenseView == null) {
                throw new IntegrationException(String.format("Missing sub license view for license: %s", versionBomLicenseView.licenseDisplay));
            }
            logger.info(String.format("subLicenseView.name: %s", subLicenseView.name));
            final String subLicenseText = hubLicense.getLicenseText(subLicenseView);
            logger.info(String.format("sub license text: %s...", truncate(subLicenseText, 100)));
            logger.info("========== CREATING (or re-using) SUB LICENSE ========");
            final AnyLicenseInfo subSpdxLicense = reUseOrCreateSpdxLicense(subLicenseView);
            subSpdxLicenses.add(subSpdxLicense);
        }
        AnyLicenseInfo componentLicense = null;
        if (versionBomLicenseView.licenseType == ComplexLicenseEnum.CONJUNCTIVE) {
            logger.info("========== CREATING CONJUNCTIVE LICENSE ========");
            componentLicense = new ConjunctiveLicenseSet(subSpdxLicenses.toArray(new AnyLicenseInfo[subSpdxLicenses.size()]));
        } else if (versionBomLicenseView.licenseType == ComplexLicenseEnum.DISJUNCTIVE) {
            logger.info("========== CREATING DISJUNCTIVE LICENSE ========");
            componentLicense = new DisjunctiveLicenseSet(subSpdxLicenses.toArray(new AnyLicenseInfo[subSpdxLicenses.size()]));
        } else {
            throw new IntegrationException(String.format("Invalid license type: %s", versionBomLicenseView.licenseType.toString()));
        }
        return componentLicense;
    }

    private AnyLicenseInfo createSimpleSpdxLicense(final LicenseView licenseView) throws IntegrationException {
        logger.info("========== CREATING SIMPLE LICENSE ========");
        AnyLicenseInfo componentLicense;
        logger.info(String.format("licenseView.name: %s", licenseView.name));
        componentLicense = reUseOrCreateSpdxLicense(licenseView);
        return componentLicense;
    }

    private AnyLicenseInfo reUseOrCreateSpdxLicense(final LicenseView licenseView) throws IntegrationException {
        logger.debug("reUseOrCreateSpdxLicense()");
        AnyLicenseInfo componentLicense;
        final String licenseText = hubLicense.getLicenseText(licenseView);
        final String licenseId = generateHash(licenseView.name, licenseText);
        final Optional<? extends ExtractedLicenseInfo> existingSpdxLicense = SpdxLicense.findExtractedLicenseInfoById(bomContainer, licenseId);
        if (existingSpdxLicense.isPresent()) {
            logger.info(String.format("Re-using license id: %s, name: %s", licenseId, licenseView.name));
            componentLicense = existingSpdxLicense.get();
        } else {
            logger.info(String.format("Unable to find existing license in document: id: %s, %s; will create a custom license and add it to the document", licenseId, licenseView.name));
            logger.info(String.format("Adding new license: ID: %s, name: %s text: %s", licenseId, licenseView.name, licenseText));
            componentLicense = new ExtractedLicenseInfo(licenseId, licenseText);
        }
        return componentLicense;
    }

    private String generateHash(final String licenseName, final String licenseText) {
        final String licenseNameText = String.format("%s::%s", licenseName, licenseText);
        String hashString = "unknown";
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(licenseNameText.getBytes());
            final byte[] digest = messageDigest.digest();
            final BigInteger bigInt = new BigInteger(1, digest);
            hashString = bigInt.toString(16);
        } catch (final NoSuchAlgorithmException e) {
            logger.warn(String.format("Error computing license text hash value: %s", e.getMessage()));
        }
        return hashString;
    }

    private String truncate(final String s, int maxLen) {
        if (s.length() <= maxLen) {
            maxLen = s.length();
        }
        return s.substring(0, maxLen);
    }

    private void logUsages(final VersionBomComponentModel bomComp) {
        final List<OriginView> origins = bomComp.getOrigins();
        logger.debug(String.format("\t# Origins: %d", origins.size()));
        for (final OriginView origin : origins) {
            logger.debug(String.format("\tOrigin: externalNamespace=%s, externalId=%s, name=%s", origin.externalNamespace, origin.externalId, origin.name));
        }
    }
}
