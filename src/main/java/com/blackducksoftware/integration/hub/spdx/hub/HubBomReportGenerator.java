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

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.spdx.SpdxHubBomReportBuilder;
import com.blackducksoftware.integration.hub.spdx.SpdxRelatedLicensedPackage;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class HubBomReportGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Hub hub;
    private SpdxHubBomReportBuilder spdxHubBomReportBuilder;

    @Value("${single.thread:false}")
    private boolean singleThread;

    @Autowired
    public void setHub(final Hub hub) {
        this.hub = hub;
    }

    @Autowired
    public void setSpdxHubBomReportBuilder(final SpdxHubBomReportBuilder spdxHubBomReportBuilder) {
        this.spdxHubBomReportBuilder = spdxHubBomReportBuilder;
    }

    public void writeReport(final PrintStream ps, final String projectName, final String projectVersion)
            throws IntegrationException {
        consumeHubProjectBom(projectName, projectVersion);
        spdxHubBomReportBuilder.writeReport(ps);
    }

    private void consumeHubProjectBom(final String projectName, final String projectVersion)
            throws IntegrationException {
        logger.info(String.format("Generating report for project %s:%s", projectName, projectVersion));
        final ProjectService projectService = hub.getProjectService();
        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectName,
                projectVersion);
        if (!projectVersionWrapper.isPresent()) {
            throw new IntegrationException(String.format("Failed to retrieve project %s version %s", projectName, projectVersion));
        }
        final Optional<String> bomUrl = projectVersionWrapper.get().getProjectVersionView().getFirstLink(ProjectVersionView.COMPONENTS_LINK);
        if (!bomUrl.isPresent()) {
            throw new IntegrationException(String.format("Failed to retrieve components link for project %s version %s", projectName, projectVersion));
        }
        spdxHubBomReportBuilder.setProject(projectVersionWrapper.get(), projectName, projectVersion, bomUrl.get());
        final List<VersionBomComponentView> bom = hub.getProjectBomService().getComponentsForProjectVersion(projectVersionWrapper.get().getProjectVersionView());

        logger.info("Creating packages");
        Stream<VersionBomComponentView> bomCompStream = null;
        if (singleThread) {
            logger.info("Conversion of BOM components to SpdxPackages: Single-threaded");
            bomCompStream = bom.stream();
        } else {
            logger.info("Conversion of BOM components to SpdxPackages: Multi-threaded");
            bomCompStream = bom.parallelStream();
        }
        final List<Optional<SpdxRelatedLicensedPackage>> pkgs = bomCompStream.map(bomComp -> toSpdx(bomComp))
                .collect(Collectors.toList());
        logger.info("Creating packages: Done");

        logger.info("Adding packages to document");
        for (final Optional<SpdxRelatedLicensedPackage> pkg : pkgs) {
            final SpdxRelatedLicensedPackage actualPkg = pkg.orElseThrow(
                    () -> new BlackDuckIntegrationException("Conversion to SPDX failed for one or more components"));
            spdxHubBomReportBuilder.addPackageToDocument(actualPkg);
        }
        logger.info("Adding packages to document: Done");

    }

    private Optional<SpdxRelatedLicensedPackage> toSpdx(final VersionBomComponentView bomComp) {
        Optional<SpdxRelatedLicensedPackage> pkg = Optional.empty();
        try {
            pkg = Optional.of(spdxHubBomReportBuilder.toSpdxRelatedLicensedPackage(bomComp));
        } catch (final IntegrationException e) {
            final String msg = String.format("Error converting BOM component %s:%s to Spdx packages: %s",
                    bomComp.getComponentName(), bomComp.getComponentVersionName(), e.getMessage());
            logger.error(msg);
        }
        return pkg;
    }
}
