from enum import Enum

class Status(Enum):

    AFFECTED = "AFFECTED"
    NOT_AFFECTED = "NOT_AFFECTED"
    PATCHED = "PATCHED"
    UNDER_INVESTIGATION = "UNDER_INVESTIGATION"

    #Affected, patch available in unassigned channel
    AFFECTED_PATCH_INAPPLICABLE = "AFFECTED_PATCH_INAPPLICABLE"

    #Affected, patch available in assigned channel
    AFFECTED_PATCH_APPLICABLE = "AFFECTED_PATCH_APPLICABLE"

    #Affected, patch available in a Product Migration target
    AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT = "AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT"

    #Affected, patches are not released for the CVE.
    AFFECTED_PATCH_UNAVAILABLE = "AFFECTED_PATCH_UNAVAILABLE"

    #Affected, patches were released for the CVE but we can't find them in any of the relevant channels.
    AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI = "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI"

    #Affected, only partial patches are available for the CVE
    AFFECTED_PARTIAL_PATCH_APPLICABLE = "AFFECTED_PARTIAL_PATCH_APPLICABLE"

