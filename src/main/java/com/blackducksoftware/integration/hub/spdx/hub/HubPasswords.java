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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HubPasswords {

    @Value("${hub.password:}")
    private String hubPasswordProperty;

    @Value("${BD_HUB_PASSWORD:}")
    private String hubPasswordEnvVar;

    @Value("${BD_HUB_TOKEN:}")
    private String hubTokenEnvVar;

    @Value("${hub.proxy.password:}")
    private String hubProxyPasswordProperty;

    @Value("${BD_HUB_PROXY_PASSWORD:}")
    private String hubProxyPasswordEnvVar;

    public String getHubPassword() {
        String hubPassword = hubPasswordEnvVar;
        if (!StringUtils.isBlank(hubPasswordProperty)) {
            hubPassword = hubPasswordProperty;
        }
        return hubPassword;
    }

    public String getHubToken() {
        return hubTokenEnvVar;
    }

    public String getHubProxyPassword() {
        String hubProxyPassword = hubProxyPasswordEnvVar;
        if (!StringUtils.isBlank(hubProxyPasswordProperty)) {
            hubProxyPassword = hubProxyPasswordProperty;
        }
        return hubProxyPassword;
    }
}
