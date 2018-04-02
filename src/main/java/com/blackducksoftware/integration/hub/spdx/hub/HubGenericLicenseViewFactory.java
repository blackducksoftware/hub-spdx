package com.blackducksoftware.integration.hub.spdx.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.component.VersionBomLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;

public class HubGenericLicenseViewFactory {
    public static HubGenericComplexLicenseView create(final ComplexLicenseView sourceLicense) {
        final List<HubGenericComplexLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericComplexLicenseView targetLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceLicense.license), Optional.ofNullable(sourceLicense.licenseDisplay), Optional.ofNullable(sourceLicense.type),
                Optional.of(targetChildren));
        for (final ComplexLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericComplexLicenseView targetChildLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceChild.license), Optional.ofNullable(sourceChild.licenseDisplay), Optional.ofNullable(sourceChild.type),
                    Optional.empty());
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }

    public static HubGenericComplexLicenseView create(final VersionBomLicenseView sourceLicense) {
        final List<HubGenericComplexLicenseView> targetChildren = new ArrayList<>(sourceLicense.licenses.size());
        final HubGenericComplexLicenseView targetLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceLicense.license), Optional.ofNullable(sourceLicense.licenseDisplay), Optional.ofNullable(sourceLicense.licenseType),
                Optional.of(targetChildren));
        for (final VersionBomLicenseView sourceChild : sourceLicense.licenses) {
            final HubGenericComplexLicenseView targetChildLicense = new HubGenericComplexLicenseView(Optional.ofNullable(sourceChild.license), Optional.ofNullable(sourceChild.licenseDisplay), Optional.ofNullable(sourceChild.licenseType),
                    Optional.empty());
            targetChildren.add(targetChildLicense);
        }
        return targetLicense;
    }
}
