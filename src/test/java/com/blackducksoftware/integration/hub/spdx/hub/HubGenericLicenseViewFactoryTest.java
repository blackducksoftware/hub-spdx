package com.blackducksoftware.integration.hub.spdx.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.generated.component.VersionBomLicenseView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.LicenseCodeSharingType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.LicenseOwnershipType;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericLicenseViewFactory;

public class HubGenericLicenseViewFactoryTest {

    @Test
    public void testCreateFromComplexLicenseView() {
        final ComplexLicenseView child1 = Mockito.mock(ComplexLicenseView.class);

        child1.license = "testUrlChild1";
        child1.codeSharing = LicenseCodeSharingType.PERMISSIVE;
        child1.name = "testNameChild1";
        child1.ownership = LicenseOwnershipType.OPEN_SOURCE;
        child1.type = null;
        child1.licenseDisplay = "testLicenseDisplayChild1";

        final ComplexLicenseView child2 = new ComplexLicenseView();
        child2.license = "testUrlChild2";
        child2.codeSharing = LicenseCodeSharingType.PERMISSIVE;
        child2.name = "testNameChild2";
        child2.ownership = LicenseOwnershipType.OPEN_SOURCE;
        child2.type = null;
        child2.licenseDisplay = "testLicenseDisplayChild2";

        final ComplexLicenseView sourceLicense = new ComplexLicenseView();
        sourceLicense.license = "testUrl";
        sourceLicense.codeSharing = LicenseCodeSharingType.PERMISSIVE;
        sourceLicense.name = "testName";
        sourceLicense.ownership = LicenseOwnershipType.OPEN_SOURCE;
        sourceLicense.type = ComplexLicenseType.CONJUNCTIVE;
        sourceLicense.licenseDisplay = "testLicenseDisplay";
        sourceLicense.licenses = new ArrayList<>();
        sourceLicense.licenses.add(child1);
        sourceLicense.licenses.add(child2);

        final HubGenericComplexLicenseView targetLicense = HubGenericLicenseViewFactory.create(sourceLicense);

        assertEquals("testLicenseDisplay", targetLicense.getDisplayName().get());
        assertEquals(ComplexLicenseType.CONJUNCTIVE, targetLicense.getType().get());
        assertEquals("testUrl", targetLicense.getUrl().get());

        assertFalse(targetLicense.getLicenses().get().get(0).getType().isPresent());
        assertEquals("testLicenseDisplayChild1", targetLicense.getLicenses().get().get(0).getDisplayName().get());
        assertEquals("testUrlChild1", targetLicense.getLicenses().get().get(0).getUrl().get());
        assertEquals(Optional.empty(), targetLicense.getLicenses().get().get(0).getLicenses());

        assertFalse(targetLicense.getLicenses().get().get(1).getType().isPresent());
        assertEquals("testLicenseDisplayChild2", targetLicense.getLicenses().get().get(1).getDisplayName().get());
        assertEquals("testUrlChild2", targetLicense.getLicenses().get().get(1).getUrl().get());
        assertEquals(Optional.empty(), targetLicense.getLicenses().get().get(1).getLicenses());
    }

    @Test
    public void testCreateFromVersionBomLicenseView() {
        final VersionBomLicenseView child1 = new VersionBomLicenseView();
        child1.license = "testUrlChild1";
        child1.licenseDisplay = "testLicenseDisplayChild1";
        child1.licenseType = null;

        final VersionBomLicenseView child2 = new VersionBomLicenseView();
        child2.license = "testUrlChild2";
        child2.licenseDisplay = "testLicenseDisplayChild2";
        child2.licenseType = null;

        final VersionBomLicenseView sourceLicense = new VersionBomLicenseView();
        sourceLicense.license = "testUrl";
        sourceLicense.licenseDisplay = "testLicenseDisplay";
        sourceLicense.licenseType = ComplexLicenseType.CONJUNCTIVE;
        sourceLicense.licenses = new ArrayList<>();
        sourceLicense.licenses.add(child1);
        sourceLicense.licenses.add(child2);

        final HubGenericComplexLicenseView targetLicense = HubGenericLicenseViewFactory.create(sourceLicense);

        assertEquals("testLicenseDisplay", targetLicense.getDisplayName().get());
        assertEquals(ComplexLicenseType.CONJUNCTIVE, targetLicense.getType().get());
        assertEquals("testUrl", targetLicense.getUrl().get());

        assertFalse(targetLicense.getLicenses().get().get(0).getType().isPresent());
        assertEquals("testLicenseDisplayChild1", targetLicense.getLicenses().get().get(0).getDisplayName().get());
        assertEquals("testUrlChild1", targetLicense.getLicenses().get().get(0).getUrl().get());
        assertEquals(Optional.empty(), targetLicense.getLicenses().get().get(0).getLicenses());

        assertFalse(targetLicense.getLicenses().get().get(1).getType().isPresent());
        assertEquals("testLicenseDisplayChild2", targetLicense.getLicenses().get().get(1).getDisplayName().get());
        assertEquals("testUrlChild2", targetLicense.getLicenses().get().get(1).getUrl().get());
        assertEquals(Optional.empty(), targetLicense.getLicenses().get().get(1).getLicenses());
    }
}
