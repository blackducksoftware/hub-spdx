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

import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;

public class HubGenericComplexLicenseView {

    private final Optional<String> url;
    private final Optional<String> displayName;
    private final Optional<ComplexLicenseType> type;
    private final Optional<List<HubGenericComplexLicenseView>> licenses;

    public HubGenericComplexLicenseView(final Optional<String> url, final Optional<String> displayName, final Optional<ComplexLicenseType> type, final Optional<List<HubGenericComplexLicenseView>> licenses) {
        this.url = url;
        this.displayName = displayName;
        this.type = type;
        this.licenses = licenses;
    }

    public Optional<String> getUrl() {
        return url;
    }

    public Optional<String> getDisplayName() {
        return displayName;
    }

    public Optional<ComplexLicenseType> getType() {
        return type;
    }

    public Optional<List<HubGenericComplexLicenseView>> getLicenses() {
        return licenses;
    }

    public boolean isLicenseNotFound() {
        if (!displayName.isPresent()) {
            return false;
        }
        if ("License Not Found".equals(displayName.get())) {
            return true;
        }
        return false;
    }

    public boolean isUnknownLicense() {
        if (!displayName.isPresent()) {
            return false;
        }
        if ("Unknown License".equals(displayName.get())) {
            return true;
        }
        return false;
    }
}
