package com.blackducksoftware.integration.hub.spdx.hub;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HubPasswords {

    @Value("${hub.password:}")
    private String hubPasswordProperty;

    @Value("${BD_HUB_PASSWORD:}")
    private String hubPasswordEnvVar;

    @Value("${hub.proxy.password:}")
    private String hubProxyPasswordProperty;

    @Value("${BD_HUB_PROXY_PASSWORD:}")
    private String hubProxyPasswordEnvVar;

    public String getHubPassword() {
        String hubPassword = hubPasswordEnvVar;
        if (!StringUtils.isBlank(hubPasswordProperty)) {
            hubPassword = hubPasswordProperty;
        }
        return hubPassword;
    }

    public String getHubProxyPassword() {
        String hubProxyPassword = hubProxyPasswordEnvVar;
        if (!StringUtils.isBlank(hubProxyPasswordProperty)) {
            hubProxyPassword = hubProxyPasswordProperty;
        }
        return hubProxyPassword;
    }
}
