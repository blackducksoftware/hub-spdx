package com.blackducksoftware.integration.hub.spdx.hub;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;

public class HubGenericLicenseViewFactory {
    public static HubGenericLicenseView create(final ComplexLicenseView sourceLicense) {
        final List<HubGenericLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericLicenseView targetLicense = new HubGenericLicenseView(sourceLicense.license, sourceLicense.licenseDisplay, sourceLicense.type, targetChildren);
        for (final ComplexLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericLicenseView targetChildLicense = new HubGenericLicenseView(sourceChild.license, sourceChild.licenseDisplay, sourceChild.type, null);
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }

    public static HubGenericLicenseView create(final VersionBomLicenseView sourceLicense) {
        final List<HubGenericLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericLicenseView targetLicense = new HubGenericLicenseView(sourceLicense.license, sourceLicense.licenseDisplay, sourceLicense.licenseType, targetChildren);
        for (final VersionBomLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericLicenseView targetChildLicense = new HubGenericLicenseView(sourceChild.license, sourceChild.licenseDisplay, sourceChild.licenseType, null);
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }
}
