package com.redhat.rhn.domain.contentmgmt.validation;

import com.redhat.rhn.domain.contentmgmt.ContentProject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates a content project instance using a specified list of validators
 */
public class ContentProjectValidator {

    private ContentProject project;
    private List<ContentValidator> validators;

    public ContentProjectValidator(ContentProject projectIn) {
        this.project = projectIn;
        this.validators = Arrays.asList(new ModularSourcesValidator(), new ModularDependencyValidator());
    }

    public List<ContentValidationMessage> validate() {
        return validators.stream()
                .flatMap(v -> v.validate(project).stream())
                .collect(Collectors.toList());
    }
}
