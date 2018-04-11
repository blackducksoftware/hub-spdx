package com.blackducksoftware.integration.hub.spdx;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ClassPathPropertiesFileTest {

    @Test
    public void test() throws IOException {
        final ClassPathPropertiesFile classPathPropertiesFile = new ClassPathPropertiesFile("version.properties");
        final String version = classPathPropertiesFile.getProperty("program.version");
        assertTrue(Character.isDigit(version.charAt(0)));
    }

}
