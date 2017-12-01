package com.blackducksoftware.integration.hub.spdx.hub;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;

public class HubConfig {
    public HubServerConfigBuilder configure(final HubServerConfigBuilder hubServerConfigBuilder, final String hubUrl, final String hubUsername, final String hubPassword, final String hubProxyHost, final int hubProxyPort,
            final String hubProxyUsername, final String hubProxyPassword, final int hubTimeoutSeconds, final boolean hubAlwaysTrustCert) {
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
