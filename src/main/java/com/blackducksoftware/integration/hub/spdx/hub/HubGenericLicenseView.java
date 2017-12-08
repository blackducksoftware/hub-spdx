package com.blackducksoftware.integration.hub.spdx.hub;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;

public class HubGenericLicenseView {

    private final String url;
    private final String displayName;
    private final ComplexLicenseEnum type;
    private List<HubGenericLicenseView> licenses = new ArrayList<>();

    public HubGenericLicenseView(final String url, final String displayName, final ComplexLicenseEnum type, final List<HubGenericLicenseView> licenses) {
        this.url = url;
        this.displayName = displayName;
        this.type = type;
        this.licenses = licenses;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ComplexLicenseEnum getType() {
        return type;
    }

    public List<HubGenericLicenseView> getLicenses() {
        return licenses;
    }

}
