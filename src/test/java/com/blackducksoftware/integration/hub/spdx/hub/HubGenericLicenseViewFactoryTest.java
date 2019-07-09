package com.blackducksoftware.integration.hub.spdx.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericComplexLicenseView;
import com.blackducksoftware.integration.hub.spdx.hub.license.HubGenericLicenseViewFactory;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ComplexLicenseType;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseOwnershipType;
import com.synopsys.integration.blackduck.api.generated.view.ComplexLicenseView;

public class HubGenericLicenseViewFactoryTest {

    @Test
    public void testCreateFromComplexLicenseView() {
        final ComplexLicenseView child1 = new ComplexLicenseView();

        child1.setLicense("testUrlChild1");
        child1.setName("testNameChild1");
        child1.setOwnership(LicenseOwnershipType.OPEN_SOURCE.name());
        child1.setLicenseDisplay("testLicenseDisplayChild1");

        final ComplexLicenseView child2 = new ComplexLicenseView();
        child2.setLicense("testUrlChild2");
        child2.setName("testNameChild2");
        child2.setOwnership(LicenseOwnershipType.OPEN_SOURCE.name());
        child2.setLicenseDisplay("testLicenseDisplayChild2");

        final ComplexLicenseView sourceLicense = new ComplexLicenseView();
        sourceLicense.setLicense("testUrl");
        sourceLicense.setName("testName");
        sourceLicense.setOwnership(LicenseOwnershipType.OPEN_SOURCE.name());
        sourceLicense.setType(ComplexLicenseType.CONJUNCTIVE);
        sourceLicense.setLicenseDisplay("testLicenseDisplay");
        final List<ComplexLicenseView> licenses = new ArrayList<>();
        licenses.add(child1);
        licenses.add(child2);
        sourceLicense.setLicenses(licenses);

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
        child1.setLicense("testUrlChild1");
        child1.setLicenseDisplay("testLicenseDisplayChild1");

        final VersionBomLicenseView child2 = new VersionBomLicenseView();
        child2.setLicense("testUrlChild2");
        child2.setLicenseDisplay("testLicenseDisplayChild2");

        final VersionBomLicenseView sourceLicense = new VersionBomLicenseView();
        sourceLicense.setLicense("testUrl");
        sourceLicense.setLicenseDisplay("testLicenseDisplay");
        sourceLicense.setLicenseType(ComplexLicenseType.CONJUNCTIVE);
        final List<VersionBomLicenseView> licenses = new ArrayList<>();
        licenses.add(child1);
        licenses.add(child2);
        sourceLicense.setLicenses(licenses);

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
