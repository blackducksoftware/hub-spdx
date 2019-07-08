/**
 * hub-spdx
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.ListedLicenses;
import org.spdx.rdfparser.license.SpdxNoneLicense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubLicense;
import com.blackducksoftware.integration.hub.spdx.hub.license.SpdxIdAwareLicenseView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ComplexLicenseType;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SpdxLicense {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${include.licenses:false}")
    private boolean includeLicenses;

    @Value("${use.spdx.org.license.data:true}")
    private boolean useSpdxOrgLicenseData;

    @Autowired
    private HubLicense hubLicense;

    private final Map<String, String> licenseIdToName = new HashMap<>();

    public void setIncludeLicenses(final boolean includeLicenses) {
        this.includeLicenses = includeLicenses;
    }

    public void setUseSpdxOrgLicenseData(final boolean useSpdxOrgLicenseData) {
        this.useSpdxOrgLicenseData = useSpdxOrgLicenseData;
    }

    public void setHubLicense(final HubLicense hubLicense) {
        this.hubLicense = hubLicense;
    }

    /**
     * Finds an extracted license in the document with the provided license ID
     */
    public Optional<? extends ExtractedLicenseInfo> findExtractedLicenseInfoById(final SpdxDocumentContainer container, final String licenseId) {
        Objects.requireNonNull(licenseId);
        return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> licenseId.equals(license.getLicenseId())).findAny();
    }

    public void put(final String licenseId, final String licenseName) {
        licenseIdToName.put(licenseId, licenseName);
    }

    public String getLicenseNameById(final String licenseId, final String defaultReturnValue) {
        final String fetchedName = licenseIdToName.get(licenseId);
        return fetchedName == null ? defaultReturnValue : fetchedName;
    }

    public AnyLicenseInfo generateLicenseInfo(final SpdxDocumentContainer bomContainer, final HubGenericComplexLicenseView hubComplexLicense) throws IntegrationException {
        AnyLicenseInfo componentLicense = new SpdxNoneLicense();
        if (!includeLicenses) {
            return componentLicense;
        }
        logger.debug(String.format("\tlicense url: %s", hubComplexLicense.getUrl()));
        if (!hubComplexLicense.getType().isPresent()) {
            componentLicense = createSimpleSpdxLicense(bomContainer, hubComplexLicense);
        } else {
            componentLicense = createComboSpdxLicense(bomContainer, hubComplexLicense);
        }
        return componentLicense;
    }

    private AnyLicenseInfo createComboSpdxLicense(final SpdxDocumentContainer bomContainer, final HubGenericComplexLicenseView hubComplexLicense) throws IntegrationException {
        logger.trace("createComboSpdxLicense()");
        if (hubComplexLicense == null) {
            logger.warn(String.format("Converting Hub license to SpdxNoneLicense, but Hub license is null"));
            return new SpdxNoneLicense();
        }
        if (hubComplexLicense.isLicenseNotFound() || hubComplexLicense.isUnknownLicense()) {
            logger.warn(String.format("Converting Hub license '%s' to SpdxNoneLicense", hubComplexLicense.getDisplayName()));
            return new SpdxNoneLicense();
        }
        logger.debug(String.format("\tlicense (%s) display: %s", hubComplexLicense.getType().toString(), hubComplexLicense.getDisplayName()));
        final List<AnyLicenseInfo> subSpdxLicenses = new ArrayList<>();
        for (final HubGenericComplexLicenseView hubSubLicenseView : hubComplexLicense.getLicenses().orElseThrow(() -> new HubIntegrationException(String.format("Missing sub-licenses for license: %s", hubComplexLicense.getDisplayName())))) {
            logger.debug(String.format("\t\tsub license url: %s", hubSubLicenseView.getUrl()));
            logger.debug(String.format("\t\tsub license display: %s", hubSubLicenseView.getDisplayName()));
            final SpdxIdAwareLicenseView subLicenseView = hubLicense.getLicenseView(hubSubLicenseView.getUrl());
            if (subLicenseView == null) {
                throw new IntegrationException(String.format("Missing sub license view for license: %s", hubComplexLicense.getDisplayName()));
            }
            logger.debug(String.format("subLicenseView.name: %s", subLicenseView.name));
            logger.debug("Creating (or re-using) sub license");
            final AnyLicenseInfo subSpdxLicense = reUseOrCreateSpdxLicense(bomContainer, subLicenseView);
            subSpdxLicenses.add(subSpdxLicense);
        }
        final Optional<ComplexLicenseType> lic = hubComplexLicense.getType();
        if (!lic.isPresent()) {
            throw new IntegrationException(String.format("License %s has no type", hubComplexLicense.getDisplayName()));
        }
        AnyLicenseInfo componentLicense = null;
        if (lic.isPresent() && lic.get() == ComplexLicenseType.CONJUNCTIVE) {
            logger.debug("creating conjunctive license");
            componentLicense = new ConjunctiveLicenseSet(subSpdxLicenses.toArray(new AnyLicenseInfo[subSpdxLicenses.size()]));
        } else if (lic.isPresent() && lic.get() == ComplexLicenseType.DISJUNCTIVE) {
            logger.debug("creating disjunctive license");
            componentLicense = new DisjunctiveLicenseSet(subSpdxLicenses.toArray(new AnyLicenseInfo[subSpdxLicenses.size()]));
        } else {
            throw new IntegrationException(String.format("Invalid license type: %s", hubComplexLicense.getType().toString()));
        }
        return componentLicense;
    }

    private AnyLicenseInfo createSimpleSpdxLicense(final SpdxDocumentContainer bomContainer, final HubGenericComplexLicenseView hubComplexLicense) throws IntegrationException {
        if (hubComplexLicense.isLicenseNotFound() || hubComplexLicense.isUnknownLicense()) {
            logger.warn(String.format("Converting Hub license '%s' to SpdxNoneLicense", hubComplexLicense.getDisplayName()));
            return new SpdxNoneLicense();
        }
        final SpdxIdAwareLicenseView licenseView = hubLicense.getLicenseView(hubComplexLicense.getUrl());
        logger.trace("creating simple license");
        logger.debug(String.format("licenseView.name: %s", licenseView.name));
        return reUseOrCreateSpdxLicense(bomContainer, licenseView);
    }

    private AnyLicenseInfo reUseOrCreateSpdxLicense(final SpdxDocumentContainer spdxDocContainer, final SpdxIdAwareLicenseView licenseView) throws IntegrationException {
        AnyLicenseInfo componentLicense = tryStandardLicense(licenseView);
        if (componentLicense == null) {
            logger.debug(String.format("Fetching license text for license %s, id: %s, from Hub", licenseView.name, licenseView.spdxId));
            final String licenseText = hubLicense.getLicenseText(licenseView);
            final String licenseId = generateLicenseId(licenseView.name, licenseText);
            logger.debug(String.format("License name: %s with license text from Hub hashed to ID: %s", licenseView.name, licenseId));
            final Optional<? extends ExtractedLicenseInfo> existingSpdxLicense = this.findExtractedLicenseInfoById(spdxDocContainer, licenseId);
            if (existingSpdxLicense.isPresent()) {
                logger.debug(String.format("Re-using license id: %s, name: %s", licenseId, licenseView.name));
                componentLicense = existingSpdxLicense.get();
            } else {
                logger.debug(String.format("Unable to find existing license in document: id: %s, %s; will create a custom license and add it to the document", licenseId, licenseView.name));
                logger.debug(String.format("Adding new license (as ExtractedLicenseInfo): ID: %s, name: %s text: %s", licenseId, licenseView.name, String.format("%s...", StringUtils.truncate(licenseText, 200))));
                componentLicense = new ExtractedLicenseInfo(licenseId, licenseText);
                this.put(licenseId, licenseView.name);
            }
        }
        return componentLicense;
    }

    private AnyLicenseInfo tryStandardLicense(final SpdxIdAwareLicenseView licenseView) {
        if (!useSpdxOrgLicenseData) {
            logger.debug("Use of spdx.org license data is disabled");
            return null;
        }
        if (StringUtils.isBlank(licenseView.spdxId)) {
            logger.info(String.format("The Hub does not have an SPDX License ID for license '%s', so will use license text from the Hub (not spdx.org)", licenseView.name));
            return null;
        }
        logger.debug(String.format("Fetching license details (as SpdxListedLicense) for license %s from spdx.org", licenseView.spdxId));
        AnyLicenseInfo componentLicense = null;
        try {
            componentLicense = ListedLicenses.getListedLicenses().getListedLicenseById(licenseView.spdxId);
        } catch (final InvalidSPDXAnalysisException e) {
            logger.warn(String.format("Error looking up SPDX License ID %s on spdx.org; will rely on the Hub for the license text instead. The lookup error was: %s", licenseView.spdxId, e.getMessage()));
        }
        return componentLicense;
    }

    private String generateLicenseId(final String licenseName, final String licenseText) {
        final String licenseNameText = String.format("%s::%s", licenseName, licenseText);
        final String generatedId = String.format("LicenseRef-%s", generateHash(licenseNameText));
        logger.debug(String.format("License name: %s; text: %s...; ID: %s", licenseName, StringUtils.truncate(licenseText, 100), generatedId));
        return generatedId;
    }

    private String generateHash(final String sourceString) {
        String hashString = "unknown";
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(sourceString.getBytes());
            final byte[] digest = messageDigest.digest();
            final BigInteger bigInt = new BigInteger(1, digest);
            hashString = bigInt.toString(16);
        } catch (final NoSuchAlgorithmException e) {
            logger.warn(String.format("Error computing license text hash value: %s", e.getMessage()));
        }
        return hashString;
    }
}
