/**
 * hub-spdx
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.spdx.SpdxRelatedLicensedPackage;

@Component
public class SpdxPkg {

    private static final Logger logger = LoggerFactory.getLogger(SpdxPkg.class);
    public static final String SPDX_URI_NAMESPACE = "http://spdx.org/rdf/terms#";
    public static final String RDFS_URI_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    public void addPackageToDocument(final SpdxDocument containingDocument, final SpdxRelatedLicensedPackage pkg) {
        try {
            addPackageToDocument(containingDocument, pkg.getPkg(), pkg.getRelType());
            if (pkg.getLicense() instanceof ExtractedLicenseInfo) {
                containingDocument.addExtractedLicenseInfos((ExtractedLicenseInfo) pkg.getLicense());
            }
            logger.debug(String.format("Added package: %s:%s", pkg.getPkg().getName(), pkg.getPkg().getVersionInfo()));
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public SpdxPackage createSpdxPackage(final AnyLicenseInfo licenseDeclared, final String pkgName, final String pkgVersion, final String downloadLocation, final RelationshipType relType) {
        try {
            final AnyLicenseInfo licenseConcluded = new SpdxNoAssertionLicense();
            final SpdxPackage pkg = new SpdxPackage(pkgName, licenseConcluded, new AnyLicenseInfo[] {} /* Licenses from files */, null /* Declared licenses */, licenseDeclared, downloadLocation, new SpdxFile[] {} /* Files */,
                    new SpdxPackageVerificationCode(null, new String[] {}));
            pkg.setLicenseInfosFromFiles(new AnyLicenseInfo[] { new SpdxNoAssertionLicense() });

            pkg.setLicenseDeclared(licenseDeclared);
            pkg.setCopyrightText("NOASSERTION");
            pkg.setFilesAnalyzed(false);
            pkg.setPackageVerificationCode(null);
            pkg.setVersionInfo(pkgVersion);
            pkg.setFilesAnalyzed(false);
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
