package com.blackducksoftware.integration.hub.spdx.spdx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ListedLicenses;
import org.spdx.rdfparser.license.SpdxListedLicense;

public class SpdxStandardLicenseTest {

    @Test
    public void test() throws InvalidSPDXAnalysisException {
        final SpdxListedLicense componentLicense = ListedLicenses.getListedLicenses().getListedLicenseById("Apache-2.0");
        assertEquals("Apache License 2.0", componentLicense.getName());
    }

}
