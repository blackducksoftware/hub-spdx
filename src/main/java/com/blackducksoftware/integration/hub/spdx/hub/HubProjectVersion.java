package com.blackducksoftware.integration.hub.spdx.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HubProjectVersion {
    @Value("${hub.project.version}")
    private String hubProjectVersion;

    @Value("${hub.project.name}")
    private String hubProjectName;

    public String getVersion() {
        return hubProjectVersion;
    }

    public String getName() {
        return hubProjectName;
    }
}
