package com.suse.oval.ovaltypes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CriteriaType.class, name = "criteriaType"),
        @JsonSubTypes.Type(value = CriterionType.class, name = "criterionType")}
)
public interface BaseCriteria extends Serializable {
}
