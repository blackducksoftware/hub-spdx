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

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProgramInfo {
    private final Logger logger = LoggerFactory.getLogger(ProgramInfo.class);
    public static final String PROGRAM_ID = "hub-spdx";
    private String version;

    @PostConstruct
    public void init() throws IOException {
        final ClassPathPropertiesFile versionProperties = new ClassPathPropertiesFile("version.properties");
        version = versionProperties.getProperty("program.version");
        logger.debug(String.format("programVersion: %s", version));
    }

    public String getVersion() {
        return version;
    }

}
