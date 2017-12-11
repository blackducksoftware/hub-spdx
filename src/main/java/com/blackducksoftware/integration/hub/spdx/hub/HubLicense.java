package com.blackducksoftware.integration.hub.spdx.hub;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class HubLicense {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Hub hub;

    public LicenseView getLicenseView(final String licenseViewUrl) throws IntegrationException {
        if (licenseViewUrl == null) {
            return null;
        }
        LicenseView licenseView = getLicenseViewSingleLevel(licenseViewUrl);
        final String embeddedLicenseUrl = (new MetaService(new Slf4jIntLogger(logger))).getFirstLinkSafely(licenseView, MetaService.LICENSE_LINK);
        if (!StringUtils.isBlank(embeddedLicenseUrl)) {
            logger.debug(String.format("Found embedded license URL: %s; fetching that licenseView", embeddedLicenseUrl));
            licenseView = getLicenseViewSingleLevel(embeddedLicenseUrl);
        }
        return licenseView;
    }

    private LicenseView getLicenseViewSingleLevel(final String licenseViewUrl) throws IntegrationException {
        if (licenseViewUrl == null) {
            return null;
        }
        logger.trace(String.format("before hub.getLicenseDataService().getLicenseView(%s)", licenseViewUrl));
        final LicenseView licenseView = hub.getLicenseDataService().getLicenseView(licenseViewUrl);
        logger.trace(String.format("after hub.getLicenseDataService().getLicenseView(%s)", licenseViewUrl));
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        logger.trace("before hub.getLicenseDataService().getLicenseText(licenseView)");
        final String licenseText = hub.getLicenseDataService().getLicenseText(licenseView);
        logger.trace("after hub.getLicenseDataService().getLicenseText(licenseView)");
        return licenseText;
    }
}
