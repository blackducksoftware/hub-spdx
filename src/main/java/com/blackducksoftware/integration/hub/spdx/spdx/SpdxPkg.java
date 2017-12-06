package com.blackducksoftware.integration.hub.spdx.spdx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpdxPkg {
    @Autowired
    private SpdxLicense spdxLicense;

    private static final Logger logger = LoggerFactory.getLogger(SpdxPkg.class);
    public static final String SPDX_URI_NAMESPACE = "http://spdx.org/rdf/terms#";
    public static final String RDFS_URI_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    public void setSpdxLicense(final SpdxLicense spdxLicense) {
        this.spdxLicense = spdxLicense;
    }

    /**
     * Creates a new package with the specified license, name, comment, and root path.
     */
    public SpdxPackage addPackageToDocument(final SpdxDocument containingDocument, final AnyLicenseInfo licenseDeclared, final String pkgName, final String pkgVersion, final String downloadLocation, final RelationshipType relType) {
        try {
            final AnyLicenseInfo licenseConcluded = new SpdxNoAssertionLicense();
            final SpdxPackage pkg = new SpdxPackage(pkgName, licenseConcluded, new AnyLicenseInfo[] {} /* Licenses from files */, null /* Declared licenses */, licenseDeclared, downloadLocation, new SpdxFile[] {} /* Files */,
                    new SpdxPackageVerificationCode(null, new String[] {}));
            pkg.setLicenseInfosFromFiles(new AnyLicenseInfo[] { new SpdxNoAssertionLicense() });

            pkg.setLicenseDeclared(licenseDeclared);
            pkg.setCopyrightText("NOASSERTION");
            pkg.setFilesAnalyzed(false);
            pkg.setPackageVerificationCode(null);
            addPackageToDocument(containingDocument, pkg, relType);
            String licenseId = licenseDeclared.toString();
            if (licenseDeclared instanceof ExtractedLicenseInfo) {
                licenseId = ((ExtractedLicenseInfo) licenseDeclared).getLicenseId();
                containingDocument.addExtractedLicenseInfos((ExtractedLicenseInfo) licenseDeclared);
            }
            pkg.setVersionInfo(pkgVersion);
            pkg.setFilesAnalyzed(false);
            pkg.setCopyrightText("NOASSERTION");
            logger.info(String.format("Added package: %s:%s, license: %s", pkgName, pkgVersion, spdxLicense.getLicenseNameById(licenseId, licenseId)));
            return pkg;
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPackageToDocument(final SpdxDocument document, final SpdxPackage pkg, final RelationshipType relType) {
        try {
            document.addRelationship(new Relationship(pkg, relType, null));
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException("Unable to add package to document", e);
        }
    }
}
