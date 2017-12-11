package com.blackducksoftware.integration.hub.spdx.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.LicenseView;

@Component
public class HubLicense {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Hub hub;

    public LicenseView getLicenseView(final String licenseViewUrl) throws IntegrationException {
        logger.trace("before hub.getLicenseDataService().getLicenseView(licenseViewUrl)");
        final LicenseView licenseView = hub.getLicenseDataService().getLicenseView(licenseViewUrl);
        logger.trace("after hub.getLicenseDataService().getLicenseView(licenseViewUrl)");
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        logger.trace("before hub.getLicenseDataService().getLicenseText(licenseView)");
        final String licenseText = hub.getLicenseDataService().getLicenseText(licenseView);
        logger.trace("after hub.getLicenseDataService().getLicenseText(licenseView)");
        return licenseText;
    }
}
