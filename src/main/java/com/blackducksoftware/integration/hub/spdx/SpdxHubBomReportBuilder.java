/**
 * hub-spdx
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import com.blackducksoftware.integration.hub.api.generated.component.VersionBomLicenseView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericLicenseViewFactory;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxLicense;
import com.blackducksoftware.integration.hub.spdx.spdx.SpdxPkg;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.net.UrlEscapers;

@Component
public class SpdxHubBomReportBuilder {

	private static final String NO_ASSERTION = "NOASSERTION";

	private SpdxPkg spdxPkg;
	private SpdxLicense spdxLicense;

	private static final String TOOL_NAME = "Tool: Black Duck Hub SPDX Report Generator";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final String SPDX_VERSION = "2.1";
	private static final String SPDX_SPEC_VERSION_ID = String.format("SPDX-%s", SPDX_VERSION);
	private SpdxDocumentContainer bomContainer;
	private SpdxDocument bomDocument;

	@Autowired
	public void setSpdxLicense(final SpdxLicense spdxLicense) {
		this.spdxLicense = spdxLicense;
	}

	@Autowired
	public void setSpdxPkg(final SpdxPkg spdxPkg) {
		this.spdxPkg = spdxPkg;
	}

	public void setProject(final ProjectVersionWrapper projectVersionWrapper, String projectName, String projectVersion,
			final String bomUrl) throws HubIntegrationException {
		bomContainer = null;
		try {
			bomContainer = new SpdxDocumentContainer(
					"http://blackducksoftware.com/hub/" //Include project and version in document URL
							+ UrlEscapers.urlFragmentEscaper().escape(projectName) + "/"
							+ UrlEscapers.urlFragmentEscaper().escape(projectVersion),
					SPDX_SPEC_VERSION_ID);
		} catch (final InvalidSPDXAnalysisException e1) {
			throw new HubIntegrationException("Error creating SPDX container", e1);
		}
		bomDocument = bomContainer.getSpdxDocument();
		try {
			bomDocument.getCreationInfo().setCreators(new String[] { TOOL_NAME });
		} catch (final InvalidSPDXAnalysisException e) {
			throw new HubIntegrationException("Error setting creator on SPDX document", e);
		}
		bomDocument.setName(String.format("%s:%s Bill Of Materials", projectVersionWrapper.getProjectView().name,
				projectVersionWrapper.getProjectVersionView().versionName));
		final Relationship description = createDocumentDescription(projectVersionWrapper, bomUrl);
		try {
			bomDocument.addRelationship(description);
		} catch (final InvalidSPDXAnalysisException e) {
			throw new HubIntegrationException("Error adding describes relationship to SPDX document", e);
		}
	}

	public void addPackageToDocument(final SpdxRelatedLicensedPackage pkg) {
		spdxPkg.addPackageToDocument(bomDocument, pkg);
	}

	public SpdxRelatedLicensedPackage toSpdxRelatedLicensedPackage(final VersionBomComponentView bomComp)
			throws IntegrationException {
		logger.info(String.format("Converting component %s:%s to SpdxPackage", bomComp.componentName,
				bomComp.componentVersionName));
		HubGenericComplexLicenseView hubGenericLicenseView = null;
		final List<VersionBomLicenseView> licenses = bomComp.licenses;
		if (licenses == null || licenses.isEmpty()) {
			logger.warn(String.format("The Hub provided no license information for BOM component %s/%s",
					bomComp.componentName, bomComp.componentVersionName));
		} else {
			logger.debug(String.format("\tComponent %s:%s, license: %s", bomComp.componentName,
					bomComp.componentVersionName, licenses.get(0).licenseDisplay));
			hubGenericLicenseView = HubGenericLicenseViewFactory.create(licenses.get(0));
		}
		final AnyLicenseInfo compSpdxLicense = spdxLicense.generateLicenseInfo(bomContainer, hubGenericLicenseView);
		logger.debug(String.format("Creating package for %s:%s", bomComp.componentName, bomComp.componentVersionName));
		final String bomCompDownloadLocation = NO_ASSERTION;
		final RelationshipType relType = getRelationshipType(bomComp);

		final SpdxPackage pkg = spdxPkg.createSpdxPackage(compSpdxLicense, bomComp.componentName,
				bomComp.componentVersionName, bomCompDownloadLocation, relType);
		return new SpdxRelatedLicensedPackage(relType, pkg, compSpdxLicense);
	}

	public String generateReportAsString() throws HubIntegrationException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream printStream = null;
		try {
			try {
				printStream = new PrintStream(outputStream, true, "utf-8");
			} catch (final UnsupportedEncodingException e) {
				throw new HubIntegrationException("Error creating PrintStream", e);
			}
			writeReport(printStream);
		} finally {
			if (printStream != null) {
				printStream.flush();
				printStream.close();
			}
		}
		return outputStream.toString();
	}

	public void writeReport(final PrintStream ps) {
		bomContainer.getModel().write(ps, "RDF/XML");
	}

	private RelationshipType getRelationshipType(final VersionBomComponentView bomComp) {
		RelationshipType relType = RelationshipType.OTHER;
		final List<MatchedFileUsagesType> usages = bomComp.usages;
		if (usages.size() > 1) {
			logger.warn(String.format("# Usages for component %s:%s is > 1: %d; only the first is used",
					bomComp.componentName, bomComp.componentVersionName, usages.size()));
		}
		if (usages.size() > 0) {
			if (usages.get(0) == MatchedFileUsagesType.DYNAMICALLY_LINKED) {
				relType = RelationshipType.DYNAMIC_LINK;
			} else if (usages.get(0) == MatchedFileUsagesType.STATICALLY_LINKED) {
				relType = RelationshipType.STATIC_LINK;
			} else if (usages.get(0) == MatchedFileUsagesType.SOURCE_CODE) {
				relType = RelationshipType.GENERATED_FROM;
			} else if (usages.get(0) == MatchedFileUsagesType.DEV_TOOL_EXCLUDED) {
				relType = RelationshipType.BUILD_TOOL_OF;
			} else if (usages.get(0) == MatchedFileUsagesType.IMPLEMENTATION_OF_STANDARD) {
				relType = RelationshipType.DESCRIBED_BY;
			} else if (usages.get(0) == MatchedFileUsagesType.SEPARATE_WORK) {
				relType = RelationshipType.OTHER;
			}
		}
		return relType;
	}

	private Relationship createDocumentDescription(final ProjectVersionWrapper projectVersionWrapper,
			final String projectDownloadLocation) {
		final String hubProjectComment = null;
		final AnyLicenseInfo licenseConcluded = new SpdxNoAssertionLicense();
		final AnyLicenseInfo[] licenseInfoInFiles = new AnyLicenseInfo[] { new SpdxNoAssertionLicense() };
		final String copyrightText = null;
		final String licenseComment = null;
		final AnyLicenseInfo licenseDeclared = getProjectVersionSpdxLicense(projectVersionWrapper);
		final SpdxPackageVerificationCode packageVerificationCode = null;
		final SpdxPackage documentDescriptionPackage = new SpdxPackage(projectVersionWrapper.getProjectView().name,
				hubProjectComment, new Annotation[0], new Relationship[0], licenseConcluded, licenseInfoInFiles,
				copyrightText, licenseComment, licenseDeclared, new Checksum[0],
				projectVersionWrapper.getProjectView().description, projectDownloadLocation, new SpdxFile[0],
				"http://www.blackducksoftware.com", projectDownloadLocation, null, packageVerificationCode, null, null,
				null, projectVersionWrapper.getProjectVersionView().versionName);
		documentDescriptionPackage.setCopyrightText(NO_ASSERTION);
		documentDescriptionPackage.setSupplier(NO_ASSERTION);
		documentDescriptionPackage.setOriginator(NO_ASSERTION);
		documentDescriptionPackage.setFilesAnalyzed(false);
		return new Relationship(documentDescriptionPackage, RelationshipType.DESCRIBES, "top level comment");
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
}
