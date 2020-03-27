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
package com.blackducksoftware.integration.hub.spdx;

import java.io.File;
import java.io.PrintStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.blackducksoftware.integration.hub.spdx.hub.Hub;
import com.blackducksoftware.integration.hub.spdx.hub.HubBomReportGenerator;
import com.blackducksoftware.integration.hub.spdx.hub.HubProjectVersion;

@SpringBootApplication
public class SpdxReportUtility {
    public static final String programId = "hub-spdx";

    @Autowired
    Hub hub;

    @Autowired
    HubProjectVersion hubProjectVersion;

    @Autowired
    HubBomReportGenerator spdxReportGenerator;

    @Value("${output.filename}")
    private String outputFilename;

    private static final Logger logger = LoggerFactory.getLogger(SpdxReportUtility.class);

    public static void main(final String[] args) {
        new SpringApplicationBuilder(SpdxReportUtility.class).logStartupInfo(false).run(args);
    }

    @PostConstruct
    private void writeReport() {
        PrintStream ps = null;
        try {
            hub.connect();
            final File outputFile = new File(outputFilename);
            ps = new PrintStream(outputFile);
            spdxReportGenerator.writeReport(ps, hubProjectVersion.getName(), hubProjectVersion.getVersion());
            logger.info(String.format("Generated report file %s", outputFilename));
        } catch (final Throwable e) {
            logger.error(e.getMessage());
            final String trace = ExceptionUtils.getStackTrace(e);
            logger.debug(String.format("Stack trace: %s", trace));
            logger.info(getUsage());
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String getUsage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\nUsage: java -jar hub-spdx-<version>.jar <arguments>\n");
        sb.append("Required arguments:\n");
        sb.append("\t--hub.url=<Hub URL>\n");
        sb.append("\t--hub.username=<Hub username>\n");
        sb.append("\t--hub.project.name=<Hub project name>\n");
        sb.append("\t--hub.project.version=<Hub project version>\n");
        sb.append("\t--output.filename=<path to report file>\n");
        sb.append("\n");
        sb.append("Optional arguments:\n");
        sb.append("\t--hub.password=<Hub password>\n"); // NOSONAR
        sb.append("\t--hub.always.trust.cert=true\n");

        sb.append("\t--include.licenses=true # if true: Include license information in report\n");
        sb.append("\t--hub.proxy.host=<Proxy hostname>\n");
        sb.append("\t--hub.proxy.port=<Proxy port #>\n");
        sb.append("\t--hub.proxy.username=<Proxy username>\n");
        sb.append("\t--hub.proxy.password=<Proxy password>\n"); // NOSONAR
        sb.append("\t--hub.timeout=<# seconds> # Timeout for Hub operations\n");
        sb.append("\t--single.thread=true # if true: perform BOM component processing in a single thread\n");
        sb.append("\t--retry.count=<max # retries for get license (from Hub) operation>\n");

        sb.append("\n");
        sb.append("Optional environment variable:\n");
        sb.append("\texport BD_HUB_PASSWORD=<password>\n"); // NOSONAR
        sb.append("\n");
        sb.append("Documentation: https://github.com/blackducksoftware/hub-spdx/blob/master/README.md\n");

        return sb.toString();
    }
}
