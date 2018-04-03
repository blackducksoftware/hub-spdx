package com.blackducksoftware.integration.hub.spdx.hub.license;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.LicenseService;

public class SpdxIdAwareLicenseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HubService hubService;
    private final LicenseService licenseService;

    public SpdxIdAwareLicenseService(final HubService hubService, final LicenseService licenseService) {
        this.hubService = hubService;
        this.licenseService = licenseService;
    }

    public SpdxIdAwareLicenseView getLicenseView(final String licenseUrl) throws IntegrationException {
        if (licenseUrl == null) {
            return null;
        }
        final SpdxIdAwareLicenseView licenseView = hubService.getResponse(licenseUrl, SpdxIdAwareLicenseView.class);
        return licenseView;
    }

    public String getLicenseText(final SpdxIdAwareLicenseView licenseView) throws IntegrationException {
        logger.info(String.format("******** getLicenseText() CALLED for %s; ID: %s********", licenseView.name, licenseView.spdxId));
        return licenseService.getLicenseText(licenseView);
    }
}
