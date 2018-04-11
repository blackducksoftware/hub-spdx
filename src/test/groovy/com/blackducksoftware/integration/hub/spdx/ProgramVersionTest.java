package com.blackducksoftware.integration.hub.spdx;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ProgramVersionTest {

    @Test
    public void test() throws IOException {
        final ProgramVersion programVersion = new ProgramVersion();
        programVersion.init();
        assertTrue(Character.isDigit(programVersion.getProgramVersion().charAt(0)));
    }

}
