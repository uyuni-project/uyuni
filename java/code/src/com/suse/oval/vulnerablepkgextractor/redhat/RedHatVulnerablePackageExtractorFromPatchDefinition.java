package com.suse.oval.vulnerablepkgextractor.redhat;

import com.suse.oval.OsFamily;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.vulnerablepkgextractor.CriteriaTreeBasedExtractor;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;

import java.util.List;

public class RedHatVulnerablePackageExtractorFromPatchDefinition extends CriteriaTreeBasedExtractor {
    public RedHatVulnerablePackageExtractorFromPatchDefinition(DefinitionType patchDefinition) {
        super(patchDefinition);

        assertDefinitionIsValid();
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteriaType) {
        return null;
    }

    @Override
    protected boolean test(BaseCriteria criteria) {
        return false;
    }

    private void assertDefinitionIsValid() {
        assert definition.getDefinitionClass() == DefinitionClassEnum.PATCH &&
                definition.getOsFamily() == OsFamily.REDHAT_ENTERPRISE_LINUX;
    }
}
