package com.blackducksoftware.integration.hub.spdx.spdx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;

public class SpdxPkg {

    private static final Logger logger = LoggerFactory.getLogger(SpdxPkg.class);
    public static final String SPDX_URI_NAMESPACE = "http://spdx.org/rdf/terms#";
    public static final String RDFS_URI_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    /**
     * Creates a new package with the specified license, name, comment, and root path.
     */
    public static SpdxPackage addPackageToDocument(final SpdxDocument containingDocument, final AnyLicenseInfo declaredLicense, final String pkgName, final String downloadLocation, final RelationshipType relType) {
        try {
            final SpdxPackage pkg = new SpdxPackage(pkgName, declaredLicense, new AnyLicenseInfo[] {} /* Licenses from files */, null /* Declared licenses */, declaredLicense, downloadLocation, new SpdxFile[] {} /* Files */,
                    new SpdxPackageVerificationCode(null, new String[] {}));
            pkg.setLicenseInfosFromFiles(new AnyLicenseInfo[] { new SpdxNoAssertionLicense() });
            pkg.setCopyrightText("NOASSERTION");
            pkg.setFilesAnalyzed(false);
            pkg.setPackageVerificationCode(null);
            addPackageToDocument(containingDocument, pkg, relType);
            return pkg;
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addPackageToDocument(final SpdxDocument document, final SpdxPackage pkg, final RelationshipType relType) {
        try {
            document.addRelationship(new Relationship(pkg, relType, null));
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException("Unable to add package to document");
        }
    }
}