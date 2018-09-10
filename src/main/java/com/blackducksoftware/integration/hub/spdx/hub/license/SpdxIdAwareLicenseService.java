/**
 * hub-spdx
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.spdx.hub.license;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.LicenseService;
import com.synopsys.integration.exception.IntegrationException;

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
        return hubService.getResponse(licenseUrl, SpdxIdAwareLicenseView.class);
    }

    public String getLicenseText(final SpdxIdAwareLicenseView licenseView) throws IntegrationException {
        logger.debug(String.format("Fetching license text from Hub for license name: %s; ID: %s", licenseView.name, licenseView.spdxId));
        return licenseService.getLicenseText(licenseView);
    }
}
