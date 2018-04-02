package com.blackducksoftware.integration.hub.spdx.hub

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.blackducksoftware.integration.hub.api.generated.component.VersionBomLicenseView
import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.LicenseCodeSharingType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.LicenseOwnershipType
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView

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
        child1.codeSharing = LicenseCodeSharingType.PERMISSIVE;
        child1.name = "testNameChild1"
        child1.ownership = LicenseOwnershipType.OPEN_SOURCE
        child1.type = null
        child1.licenseDisplay = "testLicenseDisplayChild1"

        ComplexLicenseView child2 = new ComplexLicenseView();
        child2.license = "testUrlChild2"
        child2.codeSharing = LicenseCodeSharingType.PERMISSIVE;
        child2.name = "testNameChild2"
        child2.ownership = LicenseOwnershipType.OPEN_SOURCE
        child2.type = null
        child2.licenseDisplay = "testLicenseDisplayChild2"

        ComplexLicenseView sourceLicense = new ComplexLicenseView();
        sourceLicense.license = "testUrl"
        sourceLicense.codeSharing = LicenseCodeSharingType.PERMISSIVE;
        sourceLicense.name = "testName"
        sourceLicense.ownership = LicenseOwnershipType.OPEN_SOURCE
        sourceLicense.type = ComplexLicenseType.CONJUNCTIVE
        sourceLicense.licenseDisplay = "testLicenseDisplay"
        sourceLicense.licenses = new ArrayList<ComplexLicenseView>()
        sourceLicense.licenses.add(child1)
        sourceLicense.licenses.add(child2)

        HubGenericComplexLicenseView targetLicense = HubGenericLicenseViewFactory.create(sourceLicense);

        assertEquals("testLicenseDisplay", targetLicense.displayName.get());
        assertEquals(ComplexLicenseType.CONJUNCTIVE, targetLicense.type.get());
        assertEquals("testUrl", targetLicense.url.get());

        assertFalse(targetLicense.licenses.get().get(0).type.isPresent());
        assertEquals("testLicenseDisplayChild1", targetLicense.licenses.get().get(0).displayName.get());
        assertEquals("testUrlChild1", targetLicense.licenses.get().get(0).url.get());
        assertEquals(Optional.empty(), targetLicense.licenses.get().get(0).licenses);

        assertFalse(targetLicense.licenses.get().get(1).type.isPresent());
        assertEquals("testLicenseDisplayChild2", targetLicense.licenses.get().get(1).displayName.get());
        assertEquals("testUrlChild2", targetLicense.licenses.get().get(1).url.get());
        assertEquals(Optional.empty(), targetLicense.licenses.get().get(1).licenses);
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
        sourceLicense.licenseType = ComplexLicenseType.CONJUNCTIVE
        sourceLicense.licenses = new ArrayList<VersionBomLicenseView>()
        sourceLicense.licenses.add(child1)
        sourceLicense.licenses.add(child2)

        HubGenericComplexLicenseView targetLicense = HubGenericLicenseViewFactory.create(sourceLicense);

        assertEquals("testLicenseDisplay", targetLicense.displayName.get());
        assertEquals(ComplexLicenseType.CONJUNCTIVE, targetLicense.type.get());
        assertEquals("testUrl", targetLicense.url.get());

        assertFalse(targetLicense.licenses.get().get(0).type.isPresent());
        assertEquals("testLicenseDisplayChild1", targetLicense.licenses.get().get(0).displayName.get());
        assertEquals("testUrlChild1", targetLicense.licenses.get().get(0).url.get());
        assertEquals(Optional.empty(), targetLicense.licenses.get().get(0).licenses);

        assertFalse(targetLicense.licenses.get().get(1).type.isPresent());
        assertEquals("testLicenseDisplayChild2", targetLicense.licenses.get().get(1).displayName.get());
        assertEquals("testUrlChild2", targetLicense.licenses.get().get(1).url.get());
        assertEquals(Optional.empty(), targetLicense.licenses.get().get(1).licenses);
    }
}
