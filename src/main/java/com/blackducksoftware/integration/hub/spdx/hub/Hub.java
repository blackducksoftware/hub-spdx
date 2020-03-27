/**
 * hub-spdx
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.spdx.ProgramInfo;
import com.blackducksoftware.integration.hub.spdx.SpdxReportUtility;
import com.blackducksoftware.integration.hub.spdx.hub.license.SpdxIdAwareLicenseService;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectBomService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.util.NoThreadExecutorService;

@Component
public class Hub {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HubPasswords hubPasswords;

    @Autowired
    private ProgramInfo programInfo;

    @Value("${hub.url}")
    private String hubUrl;

    @Value("${hub.username}")
    private String hubUsername;

    @Value("${hub.proxy.host:}")
    private String hubProxyHost;

    @Value("${hub.proxy.port:0}")
    private int hubProxyPort;

    @Value("${hub.proxy.username:}")
    private String hubProxyUsername;

    @Value("${hub.timeout}")
    private int hubTimeoutSeconds;

    @Value("${hub.always.trust.cert}")
    private boolean hubAlwaysTrustCert;

    private BlackDuckServicesFactory hubSvcsFactory;

    public void connect() {
        final BlackDuckServerConfigBuilder hubServerConfigBuilder = new BlackDuckServerConfigBuilder();
        final HubConfig hubConfig = new HubConfig();
        final BlackDuckServerConfig hubServerConfig = hubConfig
                .configure(hubServerConfigBuilder, hubUrl, hubUsername, hubPasswords.getHubPassword(), hubPasswords.getHubToken(), hubProxyHost, hubProxyPort, hubProxyUsername, hubPasswords.getHubProxyPassword(), hubTimeoutSeconds,
                        hubAlwaysTrustCert)
                .build();
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        hubSvcsFactory = hubServerConfig.createBlackDuckServicesFactory(intLogger);
        phoneHome(hubSvcsFactory);
    }

    public ProjectBomService getProjectBomService() {
        return hubSvcsFactory.createProjectBomService();
    }

    public ProjectService getProjectService() {
        return hubSvcsFactory.createProjectService();
    }

    public SpdxIdAwareLicenseService getLicenseService() {
        return new SpdxIdAwareLicenseService(hubSvcsFactory.createBlackDuckService(), hubSvcsFactory.createLicenseService());
    }

    public String getHubUrl() {
        return hubUrl;
    }

    private void phoneHome(final BlackDuckServicesFactory hubSvcsFactory) {
        logger.trace("Phoning home");
        final BlackDuckPhoneHomeHelper phoneHomeHelper = BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(hubSvcsFactory, new NoThreadExecutorService());
        phoneHomeHelper.handlePhoneHome(ProgramInfo.PROGRAM_ID, programInfo.getVersion());
    }
}
