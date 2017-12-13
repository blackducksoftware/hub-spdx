package com.blackducksoftware.integration.hub.spdx.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;

public class HubGenericComplexLicenseView {

    private final Optional<String> url;
    private final String displayName;
    private final ComplexLicenseEnum type;
    private List<HubGenericComplexLicenseView> licenses = new ArrayList<>();

    public HubGenericComplexLicenseView(final Optional<String> url, final String displayName, final ComplexLicenseEnum type, final List<HubGenericComplexLicenseView> licenses) {
        this.url = url;
        this.displayName = displayName;
        this.type = type;
        this.licenses = licenses;
    }

    public Optional<String> getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ComplexLicenseEnum getType() {
        return type;
    }

    public List<HubGenericComplexLicenseView> getLicenses() {
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
