package com.blackducksoftware.integration.hub.spdx.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.spdx.hub.license.SpdxIdAwareLicenseService;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class Hub {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HubPasswords hubPasswords;

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

    HubServicesFactory hubSvcsFactory;

    public void connect() throws EncryptionException, IntegrationException {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        final HubConfig hubConfig = new HubConfig();
        final HubServerConfig hubServerConfig = hubConfig
                .configure(hubServerConfigBuilder, hubUrl, hubUsername, hubPasswords.getHubPassword(), hubProxyHost, hubProxyPort, hubProxyUsername, hubPasswords.getHubProxyPassword(), hubTimeoutSeconds, hubAlwaysTrustCert).build();
        final RestConnection restConnection = hubServerConfig.createCredentialsRestConnection(new Slf4jIntLogger(logger));
        restConnection.connect();
        hubSvcsFactory = new HubServicesFactory(restConnection);
    }

    public ProjectService getProjectService() {
        return hubSvcsFactory.createProjectService();
    }

    public SpdxIdAwareLicenseService getLicenseService() {
        return new SpdxIdAwareLicenseService(hubSvcsFactory.createHubService(), hubSvcsFactory.createLicenseService());
    }

    public String getHubUrl() {
        return hubUrl;
    }

}
