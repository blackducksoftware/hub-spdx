package com.blackducksoftware.integration.hub.spdx.spdx;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;

public class SpdxLicense {
    private static final Logger logger = LoggerFactory.getLogger(SpdxLicense.class);

    /**
     * Finds an extracted license in the document with the provided license ID
     */
    public static Optional<? extends ExtractedLicenseInfo> findExtractedLicenseInfoById(final SpdxDocumentContainer container, final String licenseId) {
        Objects.requireNonNull(licenseId);
        // return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> licenseId.equals(license.getLicenseId())).findAny();
        // TODO go back to line above
        logger.info(String.format("=== Checking to see if License ID '%s' already exists in document", licenseId));
        for (final ExtractedLicenseInfo lic : container.getExtractedLicenseInfos()) {
            logger.info(String.format("=== Checking License ID: %s against: %s", licenseId, lic.getLicenseId()));
            if (licenseId.equals(lic.getLicenseId())) {
                return Optional.of(lic);
            }
        }
        return Optional.empty();
    }

    public static Optional<? extends ExtractedLicenseInfo> findExtractedLicenseByNameAndText(final SpdxDocumentContainer container, final String name, final String text) {
        return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> StringUtils.equals(license.getName(), name)).filter(license -> StringUtils.equals(license.getExtractedText(), text)).findAny();

    }
}
