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
package com.blackducksoftware.integration.hub.spdx;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxPackage;

public class SpdxRelatedLicensedPackage {
    final RelationshipType relType;
    final SpdxPackage pkg;
    final AnyLicenseInfo license;

    public SpdxRelatedLicensedPackage(final RelationshipType relType, final SpdxPackage pkg, final AnyLicenseInfo license) {
        this.relType = relType;
        this.pkg = pkg;
        this.license = license;
    }

    public RelationshipType getRelType() {
        return relType;
    }

    public SpdxPackage getPkg() {
        return pkg;
    }

    public AnyLicenseInfo getLicense() {
        return license;
    }
}
