package com.blackducksoftware.integration.hub.spdx.hub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;

@Component
public class HubLicense {

    @Autowired
    private Hub hub;

    public LicenseView getLicenseView(final VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        final LicenseView licenseView = hub.getLicenseDataService().getLicenseView(versionBomLicenseView);
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        final String licenseText = hub.getLicenseDataService().getLicenseText(licenseView);
        return licenseText;
    }
}
