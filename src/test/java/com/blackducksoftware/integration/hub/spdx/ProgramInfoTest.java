package com.blackducksoftware.integration.hub.spdx;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ProgramInfoTest {

    @Test
    public void test() throws IOException {
        final ProgramInfo programInfo = new ProgramInfo();
        programInfo.init();
        assertTrue(Character.isDigit(programInfo.getVersion().charAt(0)));
    }

}
