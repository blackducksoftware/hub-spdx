package com.blackducksoftware.integration.hub.spdx.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;

@Component
public class HubLicense {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Hub hub;

    public LicenseView getLicenseView(final VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        logger.trace("before hub.getLicenseDataService().getLicenseView(versionBomLicenseView)");
        final LicenseView licenseView = hub.getLicenseDataService().getLicenseView(versionBomLicenseView);
        logger.trace("after hub.getLicenseDataService().getLicenseView(versionBomLicenseView)");
        return licenseView;
    }

    public LicenseView getLicenseView(final ComplexLicenseView complexLicenseView) throws IntegrationException {
        logger.trace("before hub.getLicenseDataService().getLicenseView(versionBomLicenseView)");
        final LicenseView licenseView = hub.getLicenseDataService().getLicenseView(complexLicenseView);
        logger.trace("after hub.getLicenseDataService().getLicenseView(versionBomLicenseView)");
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        logger.trace("before hub.getLicenseDataService().getLicenseText(licenseView)");
        final String licenseText = hub.getLicenseDataService().getLicenseText(licenseView);
        logger.trace("after hub.getLicenseDataService().getLicenseText(licenseView)");
        return licenseText;
    }
}
