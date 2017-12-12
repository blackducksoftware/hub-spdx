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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoneLicense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.HubLicense;

@Component
public class SpdxLicense {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${include.licenses:false}")
    private boolean includeLicenses;

    @Autowired
    private HubLicense hubLicense;

    private final Map<String, String> licenseIdToName = new HashMap<>();

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
        if (hubComplexLicense.getType() == null) {
            componentLicense = createSimpleSpdxLicense(bomContainer, hubComplexLicense);
        } else {
            componentLicense = createComboSpdxLicense(bomContainer, hubComplexLicense);
        }
        return componentLicense;
    }

    private AnyLicenseInfo createComboSpdxLicense(final SpdxDocumentContainer bomContainer, final HubGenericComplexLicenseView hubComplexLicense) throws IntegrationException {
        logger.trace("createComboSpdxLicense()");
        if ((hubComplexLicense == null) || (hubComplexLicense.isLicenseNotFound()) || (hubComplexLicense.isUnknownLicense())) {
            logger.warn(String.format("Converting Hub license '%s' to SpdxNoneLicense", hubComplexLicense.getDisplayName()));
            return new SpdxNoneLicense();
        }
        logger.debug(String.format("\tlicense (%s) display: %s", hubComplexLicense.getType().toString(), hubComplexLicense.getDisplayName()));
        final List<AnyLicenseInfo> subSpdxLicenses = new ArrayList<>();
        for (final HubGenericComplexLicenseView hubSubLicenseView : hubComplexLicense.getLicenses()) {
            logger.debug(String.format("\t\tsub license url: %s", hubSubLicenseView.getUrl()));
            logger.debug(String.format("\t\tsub license display: %s", hubSubLicenseView.getDisplayName()));
            final LicenseView subLicenseView = hubLicense.getLicenseView(hubSubLicenseView.getUrl());
            if (subLicenseView == null) {
                throw new IntegrationException(String.format("Missing sub license view for license: %s", hubComplexLicense.getDisplayName()));
            }
            logger.debug(String.format("subLicenseView.name: %s", subLicenseView.name));
            final String subLicenseText = hubLicense.getLicenseText(subLicenseView);
            logger.debug(String.format("sub license text: %s...", truncate(subLicenseText, 200)));
            logger.debug("Creating (or re-using) sub license");
            final AnyLicenseInfo subSpdxLicense = reUseOrCreateSpdxLicense(bomContainer, subLicenseView);
            subSpdxLicenses.add(subSpdxLicense);
        }
        AnyLicenseInfo componentLicense = new SpdxNoneLicense();
        if (hubComplexLicense.getType() == ComplexLicenseEnum.CONJUNCTIVE) {
            logger.debug("creating conjunctive license");
            componentLicense = new ConjunctiveLicenseSet(subSpdxLicenses.toArray(new AnyLicenseInfo[subSpdxLicenses.size()]));
        } else if (hubComplexLicense.getType() == ComplexLicenseEnum.DISJUNCTIVE) {
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
        final LicenseView licenseView = hubLicense.getLicenseView(hubComplexLicense.getUrl());
        logger.trace("creating simple license");
        logger.debug(String.format("licenseView.name: %s", licenseView.name));
        final AnyLicenseInfo componentLicense = reUseOrCreateSpdxLicense(bomContainer, licenseView);
        return componentLicense;
    }

    private AnyLicenseInfo reUseOrCreateSpdxLicense(final SpdxDocumentContainer spdxDocContainer, final LicenseView licenseView) throws IntegrationException {
        logger.trace("reUseOrCreateSpdxLicense()");
        AnyLicenseInfo componentLicense;
        final String licenseText = hubLicense.getLicenseText(licenseView);
        final String licenseId = generateLicenseId(licenseView.name, licenseText);
        logger.debug(String.format("License name: %s with license text from Hub hashed to ID: %s", licenseView.name, licenseId));
        final Optional<? extends ExtractedLicenseInfo> existingSpdxLicense = this.findExtractedLicenseInfoById(spdxDocContainer, licenseId);
        if (existingSpdxLicense.isPresent()) {
            logger.debug(String.format("Re-using license id: %s, name: %s", licenseId, licenseView.name));
            componentLicense = existingSpdxLicense.get();
        } else {
            logger.debug(String.format("Unable to find existing license in document: id: %s, %s; will create a custom license and add it to the document", licenseId, licenseView.name));
            logger.debug(String.format("Adding new license: ID: %s, name: %s text: %s", licenseId, licenseView.name, String.format("%s...", truncate(licenseText, 200))));
            componentLicense = new ExtractedLicenseInfo(licenseId, licenseText);
            this.put(licenseId, licenseView.name);
        }
        return componentLicense;
    }

    private String generateLicenseId(final String licenseName, final String licenseText) {
        final String licenseNameText = String.format("%s::%s", licenseName, licenseText);
        final String generatedId = String.format("LicenseRef-%s", generateHash(licenseNameText));
        logger.debug(String.format("License name: %s; text: %s...; ID: %s", licenseName, truncate(licenseText, 100), generatedId));
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

    private String truncate(final String s, int maxLen) {
        if (s.length() <= maxLen) {
            maxLen = s.length();
        }
        return s.substring(0, maxLen);
    }
}
