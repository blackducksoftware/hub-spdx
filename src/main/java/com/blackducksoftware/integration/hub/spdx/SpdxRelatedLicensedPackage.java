package com.blackducksoftware.integration.hub.spdx;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxPackage;

public class SpdxRelatedLicensedPackage {
    final RelationshipType relType;
    final SpdxPackage pkg;
    final AnyLicenseInfo license;

    public SpdxRelatedLicensedPackage(final RelationshipType relType, final SpdxPackage pkg, final AnyLicenseInfo license) {
        this.relType = relType;
        this.pkg = pkg;
        this.license = license;
    }

    public RelationshipType getRelType() {
        return relType;
    }

    public SpdxPackage getPkg() {
        return pkg;
    }

    public AnyLicenseInfo getLicense() {
        return license;
    }
}
