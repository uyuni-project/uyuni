/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

// See java/core/src/main/java/com/redhat/rhn/manager/audit/PatchStatus.java
export const PATCH_STATUS_LABEL = {
  AFFECTED_PATCH_INAPPLICABLE: {
    className: "fa-exclamation-triangle text-warning",
    label: t("Affected, patches available in channels which are not assigned"),
    description: t(
      "The client is affected by a vulnerability and we have a patch for it," +
        " but the channel(s) offering the patch are not assigned to the client."
    ),
  },
  AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT: {
    className: "fa-exclamation-triangle text-warning",
    label: t("Affected, patches available in a Product Migration target"),
    description: t(
      "The client is affected by a vulnerability and we have a patch for it," +
        " but applying the patch requires migrating the product to a newer version."
    ),
  },
  AFFECTED_FULL_PATCH_APPLICABLE: {
    className: "fa-shield text-warning",
    label: t("Affected, at least one patch available in an assigned channel"),
    description: t("Affected, at least one patch available in an assigned channel"),
  },
  NOT_AFFECTED: {
    className: "fa-circle text-success",
    label: t("Not affected"),
    description: t("The client is not affected because none of the known CVE vulnerable packages are installed."),
  },
  PATCHED: {
    className: "fa-check-circle text-success",
    label: t("Patched"),
    description: t("A patch has been successfully installed on the client."),
  },
  AFFECTED_PATCH_UNAVAILABLE: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, patch is unavailable"),
    description: t("The client is affected by a vulnerability for which a patch has not yet been released."),
  },
  AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, patch is unavailable in relevant channels"),
    description: t(
      "The client is affected by a vulnerability for which a patch has been released," +
        " but the patch can't be found in the relevant channels."
    ),
  },
  AFFECTED_PARTIAL_PATCH_APPLICABLE: {
    className: "fa-shield text-danger",
    label: t("Affected, partial patch available in assigned channel"),
    description: t(
      "The client is affected by a vulnerability and we have a patch for it," +
        " but applying the patch will only update some of the vulnerable packages."
    ),
  },
  UNKNOWN: {
    className: "fa-minus-circle text-secondary",
    label: t("Unknown, CVE metadata not available"),
    description: t(
      "It is not possible to scan the client server for CVE vulnerabilities without channels or OVAL metadata."
    ),
  },
};
