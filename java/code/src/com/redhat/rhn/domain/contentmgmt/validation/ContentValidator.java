package com.redhat.rhn.domain.contentmgmt.validation;

import com.redhat.rhn.domain.contentmgmt.ContentProject;

import java.util.List;

public interface ContentValidator {
    List<ContentValidationMessage> validate(ContentProject project);
}
