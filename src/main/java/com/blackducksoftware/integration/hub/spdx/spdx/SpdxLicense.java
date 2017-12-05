package com.blackducksoftware.integration.hub.spdx.spdx;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;

public class SpdxLicense {

    /**
     * Finds an extracted license in the document with the provided license ID
     */
    public static Optional<? extends ExtractedLicenseInfo> findExtractedLicenseInfoById(final SpdxDocumentContainer container, final String licenseId) {
        Objects.requireNonNull(licenseId);
        return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> licenseId.equals(license.getLicenseId())).findAny();
    }

    public static Optional<? extends ExtractedLicenseInfo> findExtractedLicenseByNameAndText(final SpdxDocumentContainer container, final String name, final String text) {
        return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> StringUtils.equals(license.getName(), name)).filter(license -> StringUtils.equals(license.getExtractedText(), text)).findAny();

    }
}
