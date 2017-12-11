package com.blackducksoftware.integration.hub.spdx.hub

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseCodeSharingEnum
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseOwnershipEnum
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView

class HubGenericLicenseViewFactoryTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testCreateFromComplexLicenseView() {
        ComplexLicenseView child1 = new ComplexLicenseView();
        child1.license = "testUrlChild1"
        child1.codeSharing = ComplexLicenseCodeSharingEnum.PERMISSIVE;
        child1.name = "testNameChild1"
        child1.ownership = ComplexLicenseOwnershipEnum.OPEN_SOURCE
        child1.type = null
        child1.licenseDisplay = "testLicenseDisplayChild1"

        ComplexLicenseView child2 = new ComplexLicenseView();
        child2.license = "testUrlChild2"
        child2.codeSharing = ComplexLicenseCodeSharingEnum.PERMISSIVE;
        child2.name = "testNameChild2"
        child2.ownership = ComplexLicenseOwnershipEnum.OPEN_SOURCE
        child2.type = null
        child2.licenseDisplay = "testLicenseDisplayChild2"

        ComplexLicenseView sourceLicense = new ComplexLicenseView();
        sourceLicense.license = "testUrl"
        sourceLicense.codeSharing = ComplexLicenseCodeSharingEnum.PERMISSIVE;
        sourceLicense.name = "testName"
        sourceLicense.ownership = ComplexLicenseOwnershipEnum.OPEN_SOURCE
        sourceLicense.type = ComplexLicenseEnum.CONJUNCTIVE
        sourceLicense.licenseDisplay = "testLicenseDisplay"
        sourceLicense.licenses = new ArrayList<ComplexLicenseView>()
        sourceLicense.licenses.add(child1)
        sourceLicense.licenses.add(child2)

        HubGenericLicenseView targetLicense = HubGenericLicenseViewFactory.create(sourceLicense);

        assertEquals("testLicenseDisplay", targetLicense.displayName);
        assertEquals(ComplexLicenseEnum.CONJUNCTIVE, targetLicense.type);
        assertEquals("testUrl", targetLicense.url);

        assertEquals(null, targetLicense.licenses.get(0).type);
        assertEquals("testLicenseDisplayChild1", targetLicense.licenses.get(0).displayName);
        assertEquals("testUrlChild1", targetLicense.licenses.get(0).url);
        assertEquals(null, targetLicense.licenses.get(0).licenses);

        assertEquals(null, targetLicense.licenses.get(1).type);
        assertEquals("testLicenseDisplayChild2", targetLicense.licenses.get(1).displayName);
        assertEquals("testUrlChild2", targetLicense.licenses.get(1).url);
        assertEquals(null, targetLicense.licenses.get(1).licenses);
    }

    @Test
    public void testCreateFromVersionBomLicenseView() {
        VersionBomLicenseView child1 = new VersionBomLicenseView();
        child1.license = "testUrlChild1"
        child1.licenseDisplay = "testLicenseDisplayChild1"
        child1.licenseType = null

        VersionBomLicenseView child2 = new VersionBomLicenseView();
        child2.license = "testUrlChild2"
        child2.licenseDisplay = "testLicenseDisplayChild2"
        child2.licenseType = null

        VersionBomLicenseView sourceLicense = new VersionBomLicenseView();
        sourceLicense.license = "testUrl"
        sourceLicense.licenseDisplay = "testLicenseDisplay"
        sourceLicense.licenseType = ComplexLicenseEnum.CONJUNCTIVE
        sourceLicense.licenses = new ArrayList<VersionBomLicenseView>()
        sourceLicense.licenses.add(child1)
        sourceLicense.licenses.add(child2)

        HubGenericLicenseView targetLicense = HubGenericLicenseViewFactory.create(sourceLicense);

        assertEquals("testLicenseDisplay", targetLicense.displayName);
        assertEquals(ComplexLicenseEnum.CONJUNCTIVE, targetLicense.type);
        assertEquals("testUrl", targetLicense.url);

        assertEquals(null, targetLicense.licenses.get(0).type);
        assertEquals("testLicenseDisplayChild1", targetLicense.licenses.get(0).displayName);
        assertEquals("testUrlChild1", targetLicense.licenses.get(0).url);
        assertEquals(null, targetLicense.licenses.get(0).licenses);

        assertEquals(null, targetLicense.licenses.get(1).type);
        assertEquals("testLicenseDisplayChild2", targetLicense.licenses.get(1).displayName);
        assertEquals("testUrlChild2", targetLicense.licenses.get(1).url);
        assertEquals(null, targetLicense.licenses.get(1).licenses);
    }
}
