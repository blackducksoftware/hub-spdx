/**
 * hub-spdx
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ComplexLicenseView;

public class HubGenericLicenseViewFactory {

    private HubGenericLicenseViewFactory() {
    }

    public static HubGenericComplexLicenseView create(final ComplexLicenseView sourceLicense) {
        final List<HubGenericComplexLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericComplexLicenseView targetLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceLicense.license), Optional.ofNullable(sourceLicense.licenseDisplay), Optional.ofNullable(sourceLicense.type),
                Optional.of(targetChildren));
        for (final ComplexLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericComplexLicenseView targetChildLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceChild.license), Optional.ofNullable(sourceChild.licenseDisplay), Optional.ofNullable(sourceChild.type),
                    Optional.empty());
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }

    public static HubGenericComplexLicenseView create(final VersionBomLicenseView sourceLicense) {
        final List<HubGenericComplexLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses == null ? 0 : sourceLicense.licenses.size());
        final HubGenericComplexLicenseView targetLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceLicense.license), Optional.ofNullable(sourceLicense.licenseDisplay), Optional.ofNullable(sourceLicense.licenseType),
                Optional.of(targetChildren));
        if (sourceLicense.licenses != null) {
            for (final VersionBomLicenseView sourceChild : sourceLicense.licenses) {
                final HubGenericComplexLicenseView targetChildLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceChild.license), Optional.ofNullable(sourceChild.licenseDisplay),
                        Optional.ofNullable(sourceChild.licenseType),
                        Optional.empty());
                targetChildren.add(targetChildLicense);
            }
        }
        return targetLicense;
    }
}
