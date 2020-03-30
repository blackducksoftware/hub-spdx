package com.blackducksoftware.integration.hub.spdx.hub.license;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HubGenericComplexLicenseViewTest {

    @Test
    public void testLicenseNotFoundTrue() {
        final Optional<String> displayName = Optional.of("License Not Found");

        final HubGenericComplexLicenseView hubGenericComplexLicenseView =
            new HubGenericComplexLicenseView(Optional.empty(), displayName, Optional.empty(), Optional.empty());
        assertTrue(hubGenericComplexLicenseView.isLicenseNotFound());
    }

    @Test
    public void testLicenseNotFoundFalse() {
        final Optional<String> displayName = Optional.of("Apache");

        final HubGenericComplexLicenseView hubGenericComplexLicenseView =
            new HubGenericComplexLicenseView(Optional.empty(), displayName, Optional.empty(), Optional.empty());
        assertFalse(hubGenericComplexLicenseView.isLicenseNotFound());
    }

    @Test
    public void testUnknownLicenseTrue() {
        final Optional<String> displayName = Optional.of("Unknown License");

        final HubGenericComplexLicenseView hubGenericComplexLicenseView =
            new HubGenericComplexLicenseView(Optional.empty(), displayName, Optional.empty(), Optional.empty());
        assertTrue(hubGenericComplexLicenseView.isUnknownLicense());
    }

    @Test
    public void testUnknownLicenseFalse() {
        final Optional<String> displayName = Optional.of("Apache");

        final HubGenericComplexLicenseView hubGenericComplexLicenseView =
            new HubGenericComplexLicenseView(Optional.empty(), displayName, Optional.empty(), Optional.empty());
        assertFalse(hubGenericComplexLicenseView.isUnknownLicense());
    }
}
