package com.blackducksoftware.integration.hub.spdx.hub

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder

class HubConfigTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testWithNullProxyInfo() {
        HubConfig hubConfig = new HubConfig();
        HubServerConfigBuilder cb = new HubServerConfigBuilder();
        cb = hubConfig.configure(cb, 'hubUrl', 'hubUsername', 'hubPassword', null, 0, null, null, 456, true)
        assertEquals('hubUrl', cb.hubUrl)
        assertEquals('hubUsername', cb.hubCredentials.getUsername())
        assertEquals('hubPassword', cb.hubCredentials.getDecryptedPassword())
        assertEquals(null, cb.hubProxyInfo.host)
        assertEquals(0, cb.hubProxyInfo.port)
        assertEquals(null, cb.hubProxyInfo.username)
        assertEquals(null, cb.hubProxyInfo.decryptedPassword)
        assertEquals('456', cb.timeoutSeconds)
        assertEquals(true, cb.alwaysTrustServerCertificate)
    }

    @Test
    public void testWithEmptyProxyInfo() {
        HubConfig hubConfig = new HubConfig();
        HubServerConfigBuilder cb = new HubServerConfigBuilder();
        cb = hubConfig.configure(cb, 'hubUrl', 'hubUsername', 'hubPassword', '', 0, '', '', 456, true)
        assertEquals('hubUrl', cb.hubUrl)
        assertEquals('hubUsername', cb.hubCredentials.getUsername())
        assertEquals('hubPassword', cb.hubCredentials.getDecryptedPassword())
        assertEquals(null, cb.hubProxyInfo.host)
        assertEquals(0, cb.hubProxyInfo.port)
        assertEquals(null, cb.hubProxyInfo.username)
        assertEquals(null, cb.hubProxyInfo.decryptedPassword)
        assertEquals('456', cb.timeoutSeconds)
        assertEquals(true, cb.alwaysTrustServerCertificate)
    }

    @Test
    public void testWithProxyInfo() {
        HubConfig hubConfig = new HubConfig();
        HubServerConfigBuilder cb = new HubServerConfigBuilder();
        cb = hubConfig.configure(cb, 'hubUrl', 'hubUsername', 'hubPassword', 'hubProxyHost', 123, 'hubProxyUsername', 'hubProxyPassword', 456, true)
        assertEquals('hubUrl', cb.hubUrl)
        assertEquals('hubUsername', cb.hubCredentials.getUsername())
        assertEquals('hubPassword', cb.hubCredentials.getDecryptedPassword())
        assertEquals('hubProxyHost', cb.hubProxyInfo.host)
        assertEquals(123, cb.hubProxyInfo.port)
        assertEquals('hubProxyUsername', cb.hubProxyInfo.username)
        assertEquals('hubProxyPassword', cb.hubProxyInfo.decryptedPassword)
        assertEquals('456', cb.timeoutSeconds)
        assertEquals(true, cb.alwaysTrustServerCertificate)
    }
}
