package com.blackducksoftware.integration.hub.spdx.hub.license;

import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;

public class HubGenericComplexLicenseView {

    private final Optional<String> url;
    private final Optional<String> displayName;
    private final Optional<ComplexLicenseType> type;
    private final Optional<List<HubGenericComplexLicenseView>> licenses;

    public HubGenericComplexLicenseView(final Optional<String> url, final Optional<String> displayName, final Optional<ComplexLicenseType> type, final Optional<List<HubGenericComplexLicenseView>> licenses) {
        this.url = url;
        this.displayName = displayName;
        this.type = type;
        this.licenses = licenses;
    }

    public Optional<String> getUrl() {
        return url;
    }

    public Optional<String> getDisplayName() {
        return displayName;
    }

    public Optional<ComplexLicenseType> getType() {
        return type;
    }

    public Optional<List<HubGenericComplexLicenseView>> getLicenses() {
        return licenses;
    }

    public boolean isLicenseNotFound() {
        if ("License Not Found".equals(displayName)) {
            return true;
        }
        return false;
    }

    public boolean isUnknownLicense() {
        if ("Unknown License".equals(displayName)) {
            return true;
        }
        return false;
    }
}
