/**
 * hub-spdx
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.spdx.hub;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;

public class HubConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HubServerConfigBuilder configure(final HubServerConfigBuilder hubServerConfigBuilder, final String hubUrl, final String hubUsername, final String hubPassword, final String hubToken, final String hubProxyHost,
            final int hubProxyPort,
            final String hubProxyUsername, final String hubProxyPassword, final int hubTimeoutSeconds, final boolean hubAlwaysTrustCert) {
        logger.debug(String.format("Hub URL: %s", hubUrl));
        hubServerConfigBuilder.setUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);
        hubServerConfigBuilder.setApiToken(hubToken);

        if (!StringUtils.isBlank(hubProxyHost)) {
            hubServerConfigBuilder.setProxyHost(hubProxyHost);
            hubServerConfigBuilder.setProxyPort(hubProxyPort);
            hubServerConfigBuilder.setProxyUsername(hubProxyUsername);
            hubServerConfigBuilder.setProxyPassword(hubProxyPassword);
        }

        hubServerConfigBuilder.setTimeout(hubTimeoutSeconds);
        hubServerConfigBuilder.setTrustCert(hubAlwaysTrustCert);

        return hubServerConfigBuilder;
    }
}
