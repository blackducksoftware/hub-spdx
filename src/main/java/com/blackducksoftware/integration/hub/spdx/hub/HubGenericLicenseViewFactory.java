package com.blackducksoftware.integration.hub.spdx.hub;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;

public class HubGenericLicenseViewFactory {
    public static HubGenericComplexLicenseView create(final ComplexLicenseView sourceLicense) {
        final List<HubGenericComplexLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericComplexLicenseView targetLicense = new HubGenericComplexLicenseView(sourceLicense.license, sourceLicense.licenseDisplay, sourceLicense.type, targetChildren);
        for (final ComplexLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericComplexLicenseView targetChildLicense = new HubGenericComplexLicenseView(sourceChild.license, sourceChild.licenseDisplay, sourceChild.type, null);
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }

    public static HubGenericComplexLicenseView create(final VersionBomLicenseView sourceLicense) {
        final List<HubGenericComplexLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericComplexLicenseView targetLicense = new HubGenericComplexLicenseView(sourceLicense.license, sourceLicense.licenseDisplay, sourceLicense.licenseType, targetChildren);
        for (final VersionBomLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericComplexLicenseView targetChildLicense = new HubGenericComplexLicenseView(sourceChild.license, sourceChild.licenseDisplay, sourceChild.licenseType, null);
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }
}
