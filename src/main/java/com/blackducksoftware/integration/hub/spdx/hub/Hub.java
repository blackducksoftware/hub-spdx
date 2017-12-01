package com.blackducksoftware.integration.hub.spdx.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class Hub {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HubPassword hubPassword;

    @Value("${hub.url}")
    private String hubUrl;

    @Value("${hub.username}")
    private String hubUsername;

    @Value("${hub.timeout}")
    private int hubTimeoutSeconds;

    @Value("${hub.always.trust.cert}")
    private boolean hubAlwaysTrustCert;

    HubServicesFactory hubSvcsFactory;

    public void connect() throws EncryptionException, IntegrationException {
        final HubServerConfig hubServerConfig = createBuilder().build();
        final CredentialsRestConnection restConnection;
        restConnection = hubServerConfig.createCredentialsRestConnection(new Slf4jIntLogger(logger));
        restConnection.connect();
        hubSvcsFactory = new HubServicesFactory(restConnection);
    }

    public ProjectDataService getProjectDataService() {
        return hubSvcsFactory.createProjectDataService();
    }

    public VersionBomComponentDataService getVersionBomComponentDataService() {
        return hubSvcsFactory.createVersionBomComponentDataservice();
    }

    public MetaService getMetaService() {
        return hubSvcsFactory.createMetaService();
    }

    public String getHubUrl() {
        return hubUrl;
    }

    private HubServerConfigBuilder createBuilder() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword.get());
        hubServerConfigBuilder.setTimeout(hubTimeoutSeconds);
        hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubAlwaysTrustCert);
        return hubServerConfigBuilder;
    }
}
