package com.blackducksoftware.integration.hub.spdx.hub;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;

public class HubConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HubServerConfigBuilder configure(final HubServerConfigBuilder hubServerConfigBuilder, final String hubUrl, final String hubUsername, final String hubPassword, final String hubProxyHost, final int hubProxyPort,
            final String hubProxyUsername, final String hubProxyPassword, final int hubTimeoutSeconds, final boolean hubAlwaysTrustCert) {
        logger.debug(String.format("Hub URL: %s", hubUrl));
        hubServerConfigBuilder.setHubUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);

        if (!StringUtils.isBlank(hubProxyHost)) {
            hubServerConfigBuilder.setProxyHost(hubProxyHost);
            hubServerConfigBuilder.setProxyPort(hubProxyPort);
            hubServerConfigBuilder.setProxyUsername(hubProxyUsername);
            hubServerConfigBuilder.setProxyPassword(hubProxyPassword);
        }

        hubServerConfigBuilder.setTimeout(hubTimeoutSeconds);
        hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubAlwaysTrustCert);

        return hubServerConfigBuilder;
    }
}
